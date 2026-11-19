package com.siec_acc.service.serviceImpl;

import com.siec_acc.dto.stats.DashboardDto;
import com.siec_acc.dto.response.ProjectResponseDto;
import com.siec_acc.enum_status.InvoiceStatus;            // adjust package if different
import com.siec_acc.exceptions.ResourceNotFoundException;
import com.siec_acc.repository.DashboardRepository;
import com.siec_acc.service.DashboardService;
import com.siec_acc.service.ProjectService;
import com.siec_acc.utils.IndianCurrency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Dashboard aggregation. No @Transactional on purpose: every repository call is its own short read transaction,
 * so one failing widget (e.g. alerts) can be skipped without turning the whole request into a rollback error.
 * All arithmetic is BigDecimal; the only doubles are the legacy Double columns, handled with the exact round2
 * rule PurchaseBillServiceImpl uses.
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    private static final Logger logger = LoggerFactory.getLogger(DashboardServiceImpl.class);

    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Kolkata");
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final double EPSILON = 0.005;                 // same paid/balance boundary as PurchaseBillServiceImpl
    private static final int MAX_DONUT_SLICES = 5;
    private static final int GST_DUE_DAY = 20;
    private static final BigDecimal LOW_STOCK_LEVEL = BigDecimal.valueOf(5);   // matches InventoryServiceImpl "red" band
    private static final String PO_PENDING_STATUS = "pending";   // CONFIRM against your PO status values
    private static final BigDecimal BUDGET_WARN_PCT = BigDecimal.valueOf(80);
    private static final BigDecimal BUDGET_OVER_PCT = BigDecimal.valueOf(100);
    private static final int BUDGET_SCAN_LIMIT = 5;
    private static final int PER_SOURCE_ALERT_CAP = 2;
    private static final int RECENT_RECEIPT_DAYS = 7;
    private static final String BILLS_KEY = "\u0000vendor-bills";
    private static final DateTimeFormatter MONTH_LABEL = DateTimeFormatter.ofPattern("MMM-yy", Locale.ENGLISH);
    private static final DateTimeFormatter RANGE_LABEL = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);
    private static final List<InvoiceStatus> EXCLUDED_FROM_SALES = List.of(InvoiceStatus.CANCELLED);
    private static final List<InvoiceStatus> EXCLUDED_FROM_OPEN = List.of(InvoiceStatus.CANCELLED, InvoiceStatus.PAID);
    private static final List<String> SEVERITY_ORDER = List.of("red", "amber", "purple", "blue", "green");

    private final DashboardRepository repo;
    private final ProjectService projectService;
    private final BigDecimal openingBalance;

    public DashboardServiceImpl(DashboardRepository repo,
                                ProjectService projectService,
                                @Value("${app.dashboard.opening-balance:0}") BigDecimal openingBalance) {
        this.repo = repo;
        this.projectService = projectService;
        this.openingBalance = openingBalance == null ? BigDecimal.ZERO : openingBalance;
    }

    // ==================================================================
    // period handling
    // ==================================================================
    private enum Period {
        THIS_FY("This Fiscal Year"), PREV_FY("Previous Fiscal Year"), LAST_12M("Last 12 Months");

        final String label;
        Period(String label) { this.label = label; }

        static Period parse(String raw) {
            if (raw == null || raw.isBlank()) return THIS_FY;
            String key = raw.trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
            switch (key) {
                case "THISFY": case "THISFISCALYEAR": return THIS_FY;
                case "PREVFY": case "PREVIOUSFY": case "PREVIOUSFISCALYEAR": return PREV_FY;
                case "LAST12M": case "LAST12MONTHS": return LAST_12M;
                default: throw new IllegalArgumentException("Invalid period '" + raw + "'. Allowed: THIS_FY, PREV_FY, LAST_12M.");
            }
        }
    }

    private record Range(Period period, LocalDate from, LocalDate to, List<YearMonth> months) {}

    // Indian financial year: 1 Apr - 31 Mar (same convention as invoice numbers and project codes).
    private Range resolveRange(Period period, LocalDate today) {
        YearMonth current = YearMonth.from(today);
        int fyStartYear = today.getMonthValue() >= 4 ? today.getYear() : today.getYear() - 1;
        YearMonth first;
        YearMonth last;
        if (period == Period.THIS_FY) {
            first = YearMonth.of(fyStartYear, 4);
            last = current;
        } else if (period == Period.PREV_FY) {
            first = YearMonth.of(fyStartYear - 1, 4);
            last = YearMonth.of(fyStartYear, 3);
        } else {
            first = current.minusMonths(11);
            last = current;
        }
        List<YearMonth> months = new ArrayList<>();
        for (YearMonth m = first; !m.isAfter(last); m = m.plusMonths(1)) months.add(m);
        return new Range(period, first.atDay(1), last.atEndOfMonth(), months);
    }

    private String rangeLabel(Range r) {
        return r.months().get(0).format(RANGE_LABEL) + " - " + r.months().get(r.months().size() - 1).format(RANGE_LABEL);
    }

    // ==================================================================
    // SUMMARY (stat cards + receivables + payables)
    // ==================================================================
    @Override
    public DashboardDto.Summary getSummary() {
        LocalDate today = today();
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate prevStart = monthStart.minusMonths(1);
        // like-for-like: month-to-date vs the same day range of last month
        LocalDate prevEnd = prevStart.withDayOfMonth(Math.min(today.getDayOfMonth(), prevStart.lengthOfMonth()));

        DashboardDto.Metric sales = metric(
                toMoney(repo.sumSales(monthStart, today, EXCLUDED_FROM_SALES)),
                toMoney(repo.sumSales(prevStart, prevEnd, EXCLUDED_FROM_SALES)));
        DashboardDto.Metric purchases = metric(
                toMoney(repo.sumPurchases(monthStart, today)),
                toMoney(repo.sumPurchases(prevStart, prevEnd)));

        return DashboardDto.Summary.builder()
                .asOf(today)
                .periodLabel(monthStart.format(RANGE_LABEL) + " (month to date)")
                .comparisonLabel("vs 1-" + prevEnd.getDayOfMonth() + " "
                        + prevStart.getMonth().getDisplayName(java.time.format.TextStyle.SHORT, Locale.ENGLISH))
                .sales(sales)
                .purchases(purchases)
                .receivables(aging(openInvoiceItems(), today))
                .payables(aging(openBillItems(), today))
                .bank(cashPosition())
                .gst(gst(monthStart, today, today))
                .build();
    }

    private DashboardDto.Metric metric(BigDecimal current, BigDecimal previous) {
        BigDecimal diff = current.subtract(previous);
        String direction;
        BigDecimal pct = null;
        if (previous.signum() == 0) {
            direction = current.signum() > 0 ? "NEW" : "FLAT";
        } else {
            pct = diff.multiply(HUNDRED).divide(previous.abs(), 1, RoundingMode.HALF_UP);
            direction = diff.signum() > 0 ? "UP" : diff.signum() < 0 ? "DOWN" : "FLAT";
        }
        return DashboardDto.Metric.builder()
                .value(current).compact(IndianCurrency.compact(current)).full(IndianCurrency.full(current))
                .previousValue(previous).trendPct(pct).trendDirection(direction)
                .build();
    }

    private record OpenItem(String ref, String party, LocalDate due, BigDecimal balance) {}

    private List<OpenItem> openInvoiceItems() {
        List<OpenItem> out = new ArrayList<>();
        for (Object[] r : repo.openInvoices(EXCLUDED_FROM_OPEN)) {
            BigDecimal balance = toMoney(r[3]).subtract(toMoney(r[4]));
            if (balance.signum() <= 0) continue;
            out.add(new OpenItem(str(r[0]), str(r[1]), (LocalDate) r[2], balance));
        }
        return out;
    }

    // Exactly PurchaseBillServiceImpl.balance(): max(0, round2(amount - round2(paid))), paid when <= EPSILON.
    private List<OpenItem> openBillItems() {
        List<OpenItem> out = new ArrayList<>();
        for (Object[] r : repo.openBills()) {
            double paid = round2(num(r[4]));
            double balance = Math.max(0.0, round2(num(r[3]) - paid));
            if (balance <= EPSILON) continue;
            out.add(new OpenItem(str(r[0]), str(r[1]), (LocalDate) r[2],
                    BigDecimal.valueOf(balance).setScale(2, RoundingMode.HALF_UP)));
        }
        return out;
    }

    // Overdue = due date strictly before today (same rule as deriveStatus / countByInvoiceStatusAndInvoiceDueDateBefore).
    private DashboardDto.Aging aging(List<OpenItem> items, LocalDate today) {
        BigDecimal total = BigDecimal.ZERO, overdue = BigDecimal.ZERO, current = BigDecimal.ZERO;
        int overdueCount = 0;
        LocalDate nextDue = null;
        for (OpenItem it : items) {
            total = total.add(it.balance());
            if (it.due() != null && it.due().isBefore(today)) {
                overdue = overdue.add(it.balance());
                overdueCount++;
            } else {
                current = current.add(it.balance());
                if (it.due() != null && (nextDue == null || it.due().isBefore(nextDue))) nextDue = it.due();
            }
        }
        BigDecimal overduePct = total.signum() == 0
                ? BigDecimal.ZERO.setScale(1)
                : overdue.multiply(HUNDRED).divide(total, 1, RoundingMode.HALF_UP);
        return DashboardDto.Aging.builder()
                .totalUnpaid(total).totalUnpaidCompact(IndianCurrency.compact(total)).totalUnpaidFull(IndianCurrency.full(total))
                .current(current).currentFull(IndianCurrency.full(current))
                .overdue(overdue).overdueFull(IndianCurrency.full(overdue))
                .overdueCount(overdueCount).overduePercent(overduePct)
                .nextDueDate(nextDue)
                .nextDueInDays(nextDue == null ? null : (int) ChronoUnit.DAYS.between(today, nextDue))
                .build();
    }

    // No bank ledger exists: receipts - bill payments - approved expenses (+ optional opening balance from properties).
    private DashboardDto.Cash cashPosition() {
        BigDecimal value = openingBalance
                .add(toMoney(repo.sumCustomerReceipts()))
                .subtract(toMoney(repo.sumBillPayments()))
                .subtract(toMoney(repo.sumApprovedExpensesGross()))
                .setScale(2, RoundingMode.HALF_UP);
        return DashboardDto.Cash.builder()
                .value(value).compact(IndianCurrency.compact(value)).full(IndianCurrency.full(value))
                .basis("DERIVED_CASH_FLOW")
                .build();
    }

    // Output GST replays InvoiceServiceImpl.applyTotals (discount, then tax on the discounted subtotal, 2dp HALF_UP).
    // Input GST replays PurchaseBillServiceImpl.line(): round2(qty*rate) then round2(amount*gst%/100) per line.
    // Bills created before line items existed have no item rows, so they contribute 0 input GST.
    private DashboardDto.Gst gst(LocalDate from, LocalDate to, LocalDate today) {
        BigDecimal output = BigDecimal.ZERO;
        for (Object[] r : repo.invoiceTaxRows(from, to, EXCLUDED_FROM_SALES)) {
            BigDecimal sub = toMoney(r[0]);
            BigDecimal disc = r[1] == null ? BigDecimal.ZERO : toDecimal(r[1]);
            BigDecimal tax = r[2] == null ? BigDecimal.ZERO : toDecimal(r[2]);
            BigDecimal fx = r[3] == null ? BigDecimal.ONE : toDecimal(r[3]);
            BigDecimal afterDiscount = sub.subtract(sub.multiply(disc).divide(HUNDRED, 2, RoundingMode.HALF_UP));
            BigDecimal taxAmt = afterDiscount.multiply(tax).divide(HUNDRED, 2, RoundingMode.HALF_UP);
            output = output.add(taxAmt.multiply(fx).setScale(2, RoundingMode.HALF_UP));
        }

        double inputRaw = 0.0;
        for (Object[] r : repo.billItemGstRows(from, to)) {
            int qty = r[0] == null ? 0 : ((Number) r[0]).intValue();
            double rate = r[1] == null ? 0.0 : ((Number) r[1]).doubleValue();
            double gstPct = r[2] == null ? 0.0 : ((Number) r[2]).doubleValue();
            double amount = round2(qty * rate);
            inputRaw += round2(amount * gstPct / 100.0);
        }
        BigDecimal input = BigDecimal.valueOf(round2(inputRaw)).setScale(2, RoundingMode.HALF_UP);

        BigDecimal payable = output.subtract(input);
        LocalDate due = from.plusMonths(1).withDayOfMonth(GST_DUE_DAY);
        BigDecimal abs = payable.abs();
        return DashboardDto.Gst.builder()
                .outputGst(output).inputGst(input).payable(payable)
                .compact(IndianCurrency.compact(abs)).full(IndianCurrency.full(abs))
                .direction(payable.signum() >= 0 ? "PAYABLE" : "CREDIT")
                .dueDate(due).dueInDays((int) ChronoUnit.DAYS.between(today, due))
                .build();
    }

    // ==================================================================
    // INCOME & EXPENSE chart
    // ==================================================================
    @Override
    public DashboardDto.IncomeExpense getIncomeExpense(String periodCode) {
        Period period = Period.parse(periodCode);
        Range range = resolveRange(period, today());

        Map<YearMonth, BigDecimal> income = zeroMap(range.months());
        for (Object[] r : repo.monthlySales(range.from(), range.to(), EXCLUDED_FROM_SALES)) {
            add(income, ym(r[0], r[1]), toMoney(r[2]));
        }
        // Expense = approved expenses + purchase bills (bills shown as their own slice in the donut)
        Map<YearMonth, BigDecimal> expense = zeroMap(range.months());
        for (Object[] r : repo.monthlyExpenses(range.from(), range.to())) {
            add(expense, ym(r[0], r[1]), toMoney(r[4]));
        }
        for (Object[] r : repo.monthlyPurchases(range.from(), range.to())) {
            add(expense, ym(r[0], r[1]), toMoney(r[2]));
        }

        List<String> labels = new ArrayList<>();
        List<BigDecimal> incomeList = new ArrayList<>(), expenseList = new ArrayList<>(), netList = new ArrayList<>();
        BigDecimal totalIncome = BigDecimal.ZERO, totalExpense = BigDecimal.ZERO;
        for (YearMonth m : range.months()) {
            BigDecimal inc = income.get(m), exp = expense.get(m);
            labels.add(m.format(MONTH_LABEL));
            incomeList.add(inc);
            expenseList.add(exp);
            netList.add(inc.subtract(exp));
            totalIncome = totalIncome.add(inc);
            totalExpense = totalExpense.add(exp);
        }
        BigDecimal totalNet = totalIncome.subtract(totalExpense);

        return DashboardDto.IncomeExpense.builder()
                .period(period.name()).periodLabel(period.label).rangeLabel(rangeLabel(range))
                .months(labels).income(incomeList).expense(expenseList).net(netList)
                .totalIncome(totalIncome).totalExpense(totalExpense).totalNet(totalNet)
                .totalIncomeDisplay(IndianCurrency.full(totalIncome))
                .totalExpenseDisplay(IndianCurrency.full(totalExpense))
                .totalNetDisplay(IndianCurrency.signedFull(totalNet))
                .netPositive(totalNet.signum() >= 0)
                .build();
    }

    // ==================================================================
    // TOP EXPENSES donut
    // ==================================================================
    @Override
    public DashboardDto.TopExpenses getTopExpenses(String periodCode) {
        Period period = Period.parse(periodCode);
        Range range = resolveRange(period, today());

        Map<String, BigDecimal> totals = new LinkedHashMap<>();
        Map<String, String> labels = new HashMap<>();
        for (Object[] r : repo.monthlyExpenses(range.from(), range.to())) {
            String key = str(r[2]);
            totals.merge(key, toMoney(r[4]), BigDecimal::add);
            String label = str(r[3]).trim();
            labels.putIfAbsent(key, label.isEmpty() ? "Uncategorised" : label);
        }
        BigDecimal bills = BigDecimal.ZERO;
        for (Object[] r : repo.monthlyPurchases(range.from(), range.to())) bills = bills.add(toMoney(r[2]));
        if (bills.signum() > 0) {
            totals.put(BILLS_KEY, bills);
            labels.put(BILLS_KEY, "Vendor Bills");
        }

        List<Map.Entry<String, BigDecimal>> sorted = totals.entrySet().stream()
                .filter(e -> e.getValue().signum() > 0)
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .collect(Collectors.toList());
        BigDecimal grand = sorted.stream().map(Map.Entry::getValue).reduce(BigDecimal.ZERO, BigDecimal::add);

        List<DashboardDto.ExpenseSlice> items = new ArrayList<>();
        int keep = sorted.size() > MAX_DONUT_SLICES ? MAX_DONUT_SLICES - 1 : sorted.size();
        BigDecimal others = BigDecimal.ZERO;
        for (int i = 0; i < sorted.size(); i++) {
            Map.Entry<String, BigDecimal> e = sorted.get(i);
            if (i < keep) items.add(slice(labels.get(e.getKey()), e.getValue(), grand));
            else others = others.add(e.getValue());
        }
        if (others.signum() > 0) items.add(slice("Others", others, grand));
        forceHundred(items);

        return DashboardDto.TopExpenses.builder()
                .period(period.name()).periodLabel(period.label).rangeLabel(rangeLabel(range))
                .total(grand).totalDisplay(IndianCurrency.full(grand))
                .items(items)
                .build();
    }

    private DashboardDto.ExpenseSlice slice(String label, BigDecimal amount, BigDecimal grand) {
        BigDecimal pct = grand.signum() == 0 ? BigDecimal.ZERO.setScale(1)
                : amount.multiply(HUNDRED).divide(grand, 1, RoundingMode.HALF_UP);
        return DashboardDto.ExpenseSlice.builder()
                .label(label).amount(amount).amountDisplay(IndianCurrency.full(amount)).percent(pct).build();
    }

    // Rounded percentages can add up to 99.9 / 100.1; the remainder goes to the largest slice so the legend sums to exactly 100.0.
    private void forceHundred(List<DashboardDto.ExpenseSlice> items) {
        if (items.isEmpty()) return;
        BigDecimal sum = items.stream().map(DashboardDto.ExpenseSlice::getPercent).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal diff = new BigDecimal("100.0").subtract(sum);
        if (diff.signum() != 0) {
            DashboardDto.ExpenseSlice first = items.get(0);
            first.setPercent(first.getPercent().add(diff));
        }
    }

    // ==================================================================
    // ACTIVE PROJECTS
    // ==================================================================
    // Reuses ProjectService.getProject so revenue / cost / profit match the Projects page exactly.
    @Override
    public List<DashboardDto.ActiveProject> getActiveProjects(int limit) {
        List<DashboardDto.ActiveProject> out = new ArrayList<>();
        for (String id : repo.activeProjectIds(clamp(limit, 1, 10))) {
            try {
                ProjectResponseDto p = projectService.getProject(id);
                Integer progress = p.getProgress();
                out.add(DashboardDto.ActiveProject.builder()
                        .id(p.getId()).code(p.getCode()).name(p.getName()).customer(p.getCustomer())
                        .status(p.getStatus()).projectManager(p.getPm())
                        .startDate(p.getStartDate()).endDate(p.getEndDate())
                        .progress(progress == null ? 0 : Math.max(0, Math.min(100, progress)))
                        .budget(p.getBudget()).revenue(p.getRevenue()).actualCost(p.getActualCost())
                        .profit(p.getProfit()).budgetUsagePct(p.getBudgetUsagePct())
                        .description(p.getDescription())
                        .build());
            } catch (ResourceNotFoundException ex) {
                logger.warn("Active project {} vanished while loading the dashboard", id);
            }
        }
        return out;
    }

    // ==================================================================
    // ALERTS (computed live; no alerts table). Each source fails independently.
    // ==================================================================
    @Override
    public List<DashboardDto.Alert> getAlerts(int limit) {
        int safeLimit = clamp(limit, 1, 20);
        LocalDate today = today();

        List<DashboardDto.Alert> all = new ArrayList<>();
        all.addAll(safely("low stock", this::lowStockAlerts));
        all.addAll(safely("overdue invoices", () -> overdueInvoiceAlerts(today)));
        all.addAll(safely("pending purchase orders", this::pendingPoAlerts));
        all.addAll(safely("recent receipts", this::receiptAlerts));
        all.addAll(safely("project budgets", this::budgetAlerts));

        Comparator<DashboardDto.Alert> byTime = Comparator.comparing(
                DashboardDto.Alert::getOccurredAt, Comparator.nullsLast(Comparator.reverseOrder()));
        all.sort(Comparator.comparingInt((DashboardDto.Alert a) -> SEVERITY_ORDER.indexOf(a.getSeverity())).thenComparing(byTime));
        return all.size() > safeLimit ? new ArrayList<>(all.subList(0, safeLimit)) : all;
    }

    private List<DashboardDto.Alert> safely(String source, Supplier<List<DashboardDto.Alert>> supplier) {
        try {
            return supplier.get();
        } catch (RuntimeException ex) {
            logger.error("Dashboard alerts: '{}' source failed and was skipped", source, ex);
            return List.of();
        }
    }

    private List<DashboardDto.Alert> lowStockAlerts() {
        List<DashboardDto.Alert> out = new ArrayList<>();
        for (Object[] r : repo.lowStock(LOW_STOCK_LEVEL, PER_SOURCE_ALERT_CAP)) {
            String name = str(r[0]) + (r[1] == null || str(r[1]).isBlank() ? "" : " - " + str(r[1]));
            BigDecimal stock = toDecimal(r[2]);
            String desc = stock.signum() <= 0
                    ? "Out of stock across all warehouses. Please reorder soon."
                    : "Only " + stock.stripTrailingZeros().toPlainString() + " units left across all warehouses (low-stock level: "
                    + LOW_STOCK_LEVEL.toPlainString() + "). Please reorder soon.";
            out.add(alert("LOW_STOCK", "red", "Low Stock Alert: " + name, desc, toLdt(r[3]), null));
        }
        return out;
    }

    private List<DashboardDto.Alert> overdueInvoiceAlerts(LocalDate today) {
        List<OpenItem> overdue = openInvoiceItems().stream()
                .filter(i -> i.due() != null && i.due().isBefore(today))
                .sorted(Comparator.comparing(OpenItem::due))
                .limit(PER_SOURCE_ALERT_CAP)
                .collect(Collectors.toList());
        List<DashboardDto.Alert> out = new ArrayList<>();
        for (OpenItem i : overdue) {
            long days = ChronoUnit.DAYS.between(i.due(), today);
            out.add(alert("INVOICE_OVERDUE", days > 30 ? "red" : "amber",
                    "Invoice Overdue: " + i.ref(),
                    "Invoice for " + (i.party() == null || i.party().isBlank() ? "customer" : i.party())
                            + " (" + IndianCurrency.full(i.balance()) + ") is " + days + (days == 1 ? " day" : " days") + " overdue.",
                    i.due().plusDays(1).atStartOfDay(), "/invoices/invoices.html"));
        }
        return out;
    }

    private List<DashboardDto.Alert> pendingPoAlerts() {
        List<DashboardDto.Alert> out = new ArrayList<>();
        for (Object[] r : repo.purchaseOrdersByStatus(PO_PENDING_STATUS, PER_SOURCE_ALERT_CAP)) {
            BigDecimal amount = toMoney(r[2]);
            out.add(alert("PO_APPROVAL", "blue", "Purchase Order Awaiting Approval",
                    str(r[0]) + " for " + str(r[1]) + (amount.signum() > 0 ? " (" + IndianCurrency.full(amount) + ")" : "")
                            + " needs approval.",
                    toLdt(r[3]), "/purchase-orders/purchase-orders.html"));
        }
        return out;
    }

    private List<DashboardDto.Alert> receiptAlerts() {
        LocalDateTime since = LocalDateTime.now().minusDays(RECENT_RECEIPT_DAYS);   // createdAt is stored in server time
        List<DashboardDto.Alert> out = new ArrayList<>();
        for (Object[] r : repo.recentReceipts(since, PER_SOURCE_ALERT_CAP)) {
            String who = r[4] != null && !str(r[4]).isBlank() ? str(r[4]) : (r[5] != null && !str(r[5]).isBlank() ? str(r[5]) : "a customer");
            String against = r[2] != null && !str(r[2]).isBlank() ? " against " + str(r[2]) : "";
            out.add(alert("PAYMENT_RECEIVED", "green", "Payment Received: " + IndianCurrency.full(toMoney(r[1])),
                    "Payment received from " + who + against + ".",
                    toLdt(r[3]), "/customer-payments/customer-payments.html"));
        }
        return out;
    }

    // Uses ProjectService.getProject so the % matches the Projects page; scans only the newest few active projects.
    private List<DashboardDto.Alert> budgetAlerts() {
        List<DashboardDto.Alert> out = new ArrayList<>();
        for (String id : repo.activeProjectIds(BUDGET_SCAN_LIMIT)) {
            if (out.size() >= PER_SOURCE_ALERT_CAP) break;
            try {
                ProjectResponseDto p = projectService.getProject(id);
                BigDecimal budget = p.getBudget();
                BigDecimal usage = p.getBudgetUsagePct();
                if (budget == null || budget.signum() <= 0 || usage == null || usage.compareTo(BUDGET_WARN_PCT) < 0) continue;
                boolean over = usage.compareTo(BUDGET_OVER_PCT) > 0;
                out.add(alert("PROJECT_BUDGET", over ? "red" : "purple", "Project Budget Alert: " + p.getCode(),
                        p.getName() + (over ? " has exceeded its budget (" : " budget usage reached ")
                                + usage.stripTrailingZeros().toPlainString() + "%" + (over ? " used)." : "."),
                        null, "/projects/projects.html?project=" + p.getId()));
            } catch (ResourceNotFoundException ex) {
                logger.warn("Project {} vanished while building budget alerts", id);
            }
        }
        return out;
    }

    private DashboardDto.Alert alert(String type, String severity, String title, String description,
                                     LocalDateTime occurredAt, String link) {
        return DashboardDto.Alert.builder().type(type).severity(severity).title(title)
                .description(description).occurredAt(occurredAt).link(link).build();
    }

    // ==================================================================
    // helpers
    // ==================================================================
    private LocalDate today() { return LocalDate.now(BUSINESS_ZONE); }

    private int clamp(int v, int min, int max) { return Math.max(min, Math.min(max, v)); }

    private Map<YearMonth, BigDecimal> zeroMap(List<YearMonth> months) {
        Map<YearMonth, BigDecimal> m = new LinkedHashMap<>();
        for (YearMonth ym : months) m.put(ym, BigDecimal.ZERO.setScale(2));
        return m;
    }

    private void add(Map<YearMonth, BigDecimal> map, YearMonth ym, BigDecimal amount) {
        map.computeIfPresent(ym, (k, v) -> v.add(amount));
    }

    private YearMonth ym(Object year, Object month) {
        return YearMonth.of(((Number) year).intValue(), ((Number) month).intValue());
    }

    private String str(Object o) { return o == null ? "" : String.valueOf(o); }

    private double num(Object o) { return o == null ? 0.0 : ((Number) o).doubleValue(); }

    // Same rounding PurchaseBillServiceImpl.round2 uses.
    private double round2(double n) { return Math.round(n * 100.0) / 100.0; }

    private BigDecimal toDecimal(Object o) {
        if (o == null) return BigDecimal.ZERO;
        if (o instanceof BigDecimal) return (BigDecimal) o;
        if (o instanceof Number) return BigDecimal.valueOf(((Number) o).doubleValue());
        return new BigDecimal(o.toString());
    }

    private BigDecimal toMoney(Object o) { return toDecimal(o).setScale(2, RoundingMode.HALF_UP); }

    private LocalDateTime toLdt(Object o) {
        if (o instanceof LocalDateTime) return (LocalDateTime) o;
        if (o instanceof Timestamp) return ((Timestamp) o).toLocalDateTime();
        return null;
    }

    @Override
    public DashboardDto.ProjectExport getProjectExport(String projectStrId) {
        if (projectStrId == null || projectStrId.isBlank()) {
            throw new IllegalArgumentException("Project ID is required.");
        }
        String id = projectStrId.trim();
        ProjectResponseDto project = projectService.getProject(id);   // throws ResourceNotFoundException (404) if missing
        return DashboardDto.ProjectExport.builder()
                .project(project)
                .budget(projectService.getBudget(id))
                .sales(projectService.getSales(id))
                .purchases(projectService.getPurchases(id))
                .expenses(projectService.getExpenses(id))
                .build();
    }
}