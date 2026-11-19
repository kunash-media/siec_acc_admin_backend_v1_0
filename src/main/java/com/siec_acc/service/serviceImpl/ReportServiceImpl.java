package com.siec_acc.service.serviceImpl;

import com.siec_acc.dto.response.ProjectResponseDto;
import com.siec_acc.dto.stats.ReportDto;
import com.siec_acc.entity.ProjectEntity;          // adjust package if different
import com.siec_acc.enum_status.InvoiceStatus;           // same package as in DashboardRepository
import com.siec_acc.repository.ProjectRepository;
import com.siec_acc.repository.ReportRepository;
import com.siec_acc.service.ProjectService;
import com.siec_acc.service.ReportService;
import com.siec_acc.utils.IndianCurrency;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Report aggregation. All rules mirror the existing modules (invoice tax replay, purchase-bill round2/EPSILON,
 * overdue = due date strictly before today) so report numbers tie to the Dashboard and the module pages.
 * No @Transactional on purpose: every repository call is its own short read transaction.
 */
@Service
public class ReportServiceImpl implements ReportService {

    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Kolkata");
    private static final double EPSILON = 0.005;
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal LOW_STOCK_LEVEL = BigDecimal.valueOf(20);
    private static final int DEFAULT_RANGE_DAYS = 60;
    private static final long MAX_RANGE_DAYS = 366L * 5;
    private static final int DEFAULT_PAGE_SIZE = 100;
    private static final int MAX_PAGE_SIZE = 500;
    private static final List<InvoiceStatus> EXCLUDED_SALES = List.of(InvoiceStatus.CANCELLED);
    private static final List<InvoiceStatus> EXCLUDED_OPEN = List.of(InvoiceStatus.CANCELLED, InvoiceStatus.PAID);
    private static final String[] AGE_LABELS = {"Current", "1-30 days", "31-60 days", "61-90 days", "90+ days"};
    private static final String[] AGE_KEYS = {"current", "d30", "d60", "d90", "d90p"};

    private final ReportRepository repo;
    private final ProjectRepository projectRepository;
    private final ProjectService projectService;
    private final BigDecimal openingBalance;

    public ReportServiceImpl(ReportRepository repo, ProjectRepository projectRepository, ProjectService projectService,
                             @Value("${app.dashboard.opening-balance:0}") BigDecimal openingBalance) {
        this.repo = repo;
        this.projectRepository = projectRepository;
        this.projectService = projectService;
        this.openingBalance = openingBalance == null ? BigDecimal.ZERO : openingBalance;
    }

    // ==================================================================
    // SALES
    // ==================================================================
    private record Sale(String number, LocalDate date, String customer, BigDecimal amount, BigDecimal tax,
                        BigDecimal paid, BigDecimal balance, String status) {}

    @Override
    public ReportDto.Report sales(String fromS, String toS, String groupBy, String pageS, String sizeS) {
        Range r = range(fromS, toS);
        String g = group(groupBy, List.of("date", "customer", "item", "category"));
        int page = intParam(pageS, 1, 1, 1_000_000, "page");
        int size = intParam(sizeS, DEFAULT_PAGE_SIZE, 1, MAX_PAGE_SIZE, "size");
        LocalDate today = today();

        List<Sale> sales = new ArrayList<>();
        for (Object[] a : repo.salesRows(r.from(), r.to(), EXCLUDED_SALES)) sales.add(toSale(a, today));
        BigDecimal amount = BigDecimal.ZERO, tax = BigDecimal.ZERO, due = BigDecimal.ZERO;
        for (Sale s : sales) { amount = amount.add(s.amount()); tax = tax.add(s.tax()); due = due.add(s.balance()); }

        List<ReportDto.Card> cards = List.of(
                card("invoices", "Total Invoices", "count", BigDecimal.valueOf(sales.size()), "default"),
                card("sales", "Total Sales", "money", amount, "primary"),
                card("gst", "GST Collected", "money", tax, "amber"),
                card("due", "Due Amount", "money", due, "red"));

        List<String> notes = new ArrayList<>(List.of(
                "Non-cancelled invoices by invoice date, in rupees. Amount includes GST; Tax is the GST part.",
                "Status is as of today: Paid, Partial, Unpaid, or Overdue (due date passed with a balance)."));

        List<ReportDto.Column> columns;
        List<Map<String, Object>> all = new ArrayList<>();
        Map<String, Object> totals;

        if ("customer".equals(g)) {
            Map<String, Agg> m = new LinkedHashMap<>();
            for (Sale s : sales) {
                Agg a = agg(m, s.customer());
                a.count++;
                a.amount = a.amount.add(s.amount()); a.tax = a.tax.add(s.tax());
                a.paid = a.paid.add(s.paid()); a.due = a.due.add(s.balance());
            }
            List<Agg> list = new ArrayList<>(m.values());
            list.sort((x, y) -> y.amount.compareTo(x.amount));
            columns = List.of(col("label", "Customer", "left", "text", "default"), col("count", "Invoices", "center", "int", "default"),
                    col("amount", "Amount", "right", "money", "default"), col("tax", "Tax", "right", "money", "muted"),
                    col("paid", "Paid", "right", "money", "green"), col("due", "Due", "right", "money", "red"));
            for (Agg a : list) all.add(row("label", a.label, "count", a.count, "amount", a.amount, "tax", a.tax, "paid", a.paid, "due", a.due));
            BigDecimal paid = sales.stream().map(Sale::paid).reduce(BigDecimal.ZERO, BigDecimal::add);
            totals = row("count", sales.size(), "amount", amount, "tax", tax, "paid", paid, "due", due);
        } else if ("item".equals(g) || "category".equals(g)) {
            Map<String, ItemAgg> m = new LinkedHashMap<>();
            for (Object[] a : repo.salesItemRows(r.from(), r.to(), EXCLUDED_SALES)) {
                String label = "item".equals(g) ? nameOr(a[0], "(no name)") : nameOr(a[6], "Uncategorised");
                BigDecimal line = dec(a[2]), disc = dec(a[3]), taxPct = dec(a[4]);
                BigDecimal fx = a[5] == null ? BigDecimal.ONE : dec(a[5]);
                BigDecimal afterDisc = line.subtract(line.multiply(disc).divide(HUNDRED, 2, RoundingMode.HALF_UP));
                BigDecimal taxable = afterDisc.multiply(fx).setScale(2, RoundingMode.HALF_UP);
                BigDecimal lineTax = afterDisc.multiply(taxPct).divide(HUNDRED, 2, RoundingMode.HALF_UP)
                        .multiply(fx).setScale(2, RoundingMode.HALF_UP);
                ItemAgg ia = itemAgg(m, label);
                ia.qty = ia.qty.add(dec(a[1])); ia.taxable = ia.taxable.add(taxable); ia.tax = ia.tax.add(lineTax);
            }
            notes.add("Item / category view uses invoice line items only: shipping, other charges and round-off are not included, so its total can differ from Total Sales.");
            return itemReport("sales", "Sales Report", r, g, "item".equals(g) ? "Item" : "Category", m,
                    cards, notes, salesOptions(), page, size);
        } else {
            columns = List.of(col("number", "Invoice #", "left", "text", "default"), col("date", "Date", "left", "date", "muted"),
                    col("customer", "Customer", "left", "text", "default"), col("amount", "Amount", "right", "money", "default"),
                    col("tax", "Tax", "right", "money", "muted"), col("status", "Status", "center", "status", "default"));
            for (Sale s : sales) all.add(row("number", s.number(), "date", s.date(), "customer", s.customer(),
                    "amount", s.amount(), "tax", s.tax(), "status", s.status()));
            totals = row("amount", amount, "tax", tax);
        }
        return report("sales", "Sales Report", r, null, g, salesOptions(), cards, List.of(), columns, all, totals, notes, page, size);
    }

    private Sale toSale(Object[] a, LocalDate today) {
        BigDecimal total = money(a[4]), paid = money(a[5]);
        BigDecimal sub = dec(a[6]), disc = dec(a[7]), taxPct = dec(a[8]);
        BigDecimal fx = a[9] == null ? BigDecimal.ONE : dec(a[9]);
        // Same arithmetic as InvoiceServiceImpl.applyTotals: discount, then tax on the discounted subtotal (2dp each step).
        BigDecimal afterDisc = sub.subtract(sub.multiply(disc).divide(HUNDRED, 2, RoundingMode.HALF_UP));
        BigDecimal taxDoc = afterDisc.multiply(taxPct).divide(HUNDRED, 2, RoundingMode.HALF_UP);
        BigDecimal tax = taxDoc.multiply(fx).setScale(2, RoundingMode.HALF_UP);
        BigDecimal balance = total.subtract(paid).max(BigDecimal.ZERO);
        LocalDate due = (LocalDate) a[2];
        String status = balance.signum() == 0 ? "paid"
                : (due != null && due.isBefore(today)) ? "overdue"
                : paid.signum() > 0 ? "partial" : "unpaid";
        return new Sale(str(a[0]), (LocalDate) a[1], nameOr(a[3], "—"), total, tax, paid, balance, status);
    }

    private List<ReportDto.Option> salesOptions() {
        return opts("date", "Date", "customer", "Customer", "item", "Item", "category", "Category");
    }

    // ==================================================================
    // PURCHASES
    // ==================================================================
    private record Bill(String number, LocalDate date, String vendor, BigDecimal amount, BigDecimal gst,
                        BigDecimal paid, BigDecimal balance, String status) {}

    @Override
    public ReportDto.Report purchases(String fromS, String toS, String groupBy, String pageS, String sizeS) {
        Range r = range(fromS, toS);
        String g = group(groupBy, List.of("date", "vendor", "item"));
        int page = intParam(pageS, 1, 1, 1_000_000, "page");
        int size = intParam(sizeS, DEFAULT_PAGE_SIZE, 1, MAX_PAGE_SIZE, "size");
        LocalDate today = today();

        // Per-line maths exactly like PurchaseBillServiceImpl.line(): round2(qty*rate), then round2(amount*gst%/100).
        Map<Long, double[]> lineTotals = new HashMap<>();
        Map<String, ItemAgg> items = new LinkedHashMap<>();
        for (Object[] a : repo.billItemLines(r.from(), r.to())) {
            long id = ((Number) a[0]).longValue();
            int qty = a[2] == null ? 0 : ((Number) a[2]).intValue();
            double rate = num(a[3]), gstPct = num(a[4]);
            double lineAmt = r2(qty * rate);
            double lineGst = r2(lineAmt * gstPct / 100.0);
            double[] t = lineTotals.computeIfAbsent(id, k -> new double[2]);
            t[0] += lineAmt; t[1] += lineGst;
            ItemAgg ia = itemAgg(items, nameOr(a[1], "(no name)"));
            ia.qty = ia.qty.add(BigDecimal.valueOf(qty)); ia.taxable = ia.taxable.add(dbl(lineAmt)); ia.tax = ia.tax.add(dbl(lineGst));
        }

        List<Bill> bills = new ArrayList<>();
        int noItems = 0;
        BigDecimal noItemsAmount = BigDecimal.ZERO;
        for (Object[] a : repo.billRows(r.from(), r.to())) {
            long id = ((Number) a[0]).longValue();
            LocalDate due = (LocalDate) a[3];
            double amt = num(a[5]);
            double paid = r2(num(a[6]));
            double bal = Math.max(0.0, r2(amt - paid));
            String status = (amt > 0 && bal <= EPSILON) ? "paid"
                    : (due != null && due.isBefore(today)) ? "overdue"
                    : paid > 0 ? "partial" : "received";
            double[] t = lineTotals.get(id);
            if (t == null) { noItems++; noItemsAmount = noItemsAmount.add(dbl(amt)); }
            bills.add(new Bill(str(a[1]), (LocalDate) a[2], nameOr(a[4], "—"), dbl(amt),
                    t == null ? BigDecimal.ZERO.setScale(2) : dbl(r2(t[1])), dbl(paid), dbl(bal), status));
        }

        BigDecimal amount = BigDecimal.ZERO, gst = BigDecimal.ZERO, paidSum = BigDecimal.ZERO, dueSum = BigDecimal.ZERO;
        Set<String> vendors = new LinkedHashSet<>();
        for (Bill b : bills) {
            amount = amount.add(b.amount()); gst = gst.add(b.gst()); paidSum = paidSum.add(b.paid()); dueSum = dueSum.add(b.balance());
            vendors.add(b.vendor().toLowerCase(Locale.ROOT));
        }
        List<ReportDto.Card> cards = List.of(
                card("bills", "Total Bills", "count", BigDecimal.valueOf(bills.size()), "default"),
                card("purchases", "Total Purchases", "money", amount, "primary"),
                card("gst", "Input GST", "money", gst, "amber"),
                card("vendors", "Vendors", "count", BigDecimal.valueOf(vendors.size()), "default"));

        List<String> notes = new ArrayList<>(List.of(
                "Vendor bills by bill date, in rupees. Amount includes GST; Tax is the GST on the bill line items.",
                "Status is as of today: Paid, Partial, Received (no payment yet), or Overdue."));
        if (noItems > 0) {
            notes.add(noItems + " bill(s) worth " + IndianCurrency.full(noItemsAmount)
                    + " have no line items, so their GST is not included in Input GST.");
        }

        List<ReportDto.Option> options = opts("date", "Date", "vendor", "Vendor", "item", "Item");
        List<ReportDto.Column> columns;
        List<Map<String, Object>> all = new ArrayList<>();
        Map<String, Object> totals;

        if ("vendor".equals(g)) {
            Map<String, Agg> m = new LinkedHashMap<>();
            for (Bill b : bills) {
                Agg a = agg(m, b.vendor());
                a.count++;
                a.amount = a.amount.add(b.amount()); a.tax = a.tax.add(b.gst());
                a.paid = a.paid.add(b.paid()); a.due = a.due.add(b.balance());
            }
            List<Agg> list = new ArrayList<>(m.values());
            list.sort((x, y) -> y.amount.compareTo(x.amount));
            columns = List.of(col("label", "Vendor", "left", "text", "default"), col("count", "Bills", "center", "int", "default"),
                    col("amount", "Amount", "right", "money", "default"), col("tax", "Tax", "right", "money", "muted"),
                    col("paid", "Paid", "right", "money", "green"), col("due", "Due", "right", "money", "red"));
            for (Agg a : list) all.add(row("label", a.label, "count", a.count, "amount", a.amount, "tax", a.tax, "paid", a.paid, "due", a.due));
            totals = row("count", bills.size(), "amount", amount, "tax", gst, "paid", paidSum, "due", dueSum);
        } else if ("item".equals(g)) {
            notes.add("Item view uses bill line items only. Bills without line items are not included in it.");
            return itemReport("purchases", "Purchase Report", r, g, "Item", items, cards, notes, options, page, size);
        } else {
            columns = List.of(col("number", "Bill #", "left", "text", "default"), col("date", "Date", "left", "date", "muted"),
                    col("vendor", "Vendor", "left", "text", "default"), col("amount", "Amount", "right", "money", "default"),
                    col("tax", "Tax", "right", "money", "muted"), col("status", "Status", "center", "status", "default"));
            for (Bill b : bills) all.add(row("number", b.number(), "date", b.date(), "vendor", b.vendor(),
                    "amount", b.amount(), "tax", b.gst(), "status", b.status()));
            totals = row("amount", amount, "tax", gst);
        }
        return report("purchase", "Purchase Report", r, null, g, options, cards, List.of(), columns, all, totals, notes, page, size);
    }

    // ==================================================================
    // STOCK
    // ==================================================================
    private record StockLine(String item, String sku, String category, String unit, String warehouse,
                             BigDecimal qty, BigDecimal price, BigDecimal value, String status) {}

    @Override
    public ReportDto.Report stock(String groupBy, String pageS, String sizeS) {
        String g = group(groupBy, List.of("item", "category", "warehouse"));
        int page = intParam(pageS, 1, 1, 1_000_000, "page");
        int size = intParam(sizeS, DEFAULT_PAGE_SIZE, 1, MAX_PAGE_SIZE, "size");
        LocalDate today = today();

        List<StockLine> lines = new ArrayList<>();
        int noPrice = 0;
        for (Object[] a : repo.stockRows()) {
            String name = nameOr(a[0], "(no name)");
            String variant = str(a[1]).trim();
            BigDecimal qty = dec(a[6]).setScale(2, RoundingMode.HALF_UP);
            BigDecimal price = a[7] == null ? null : dec(a[7]).setScale(2, RoundingMode.HALF_UP);
            if (price == null) noPrice++;
            BigDecimal value = price == null ? BigDecimal.ZERO.setScale(2) : qty.multiply(price).setScale(2, RoundingMode.HALF_UP);
            String status = qty.signum() <= 0 ? "red" : qty.compareTo(LOW_STOCK_LEVEL) <= 0 ? "amber" : "green";
            lines.add(new StockLine(variant.isEmpty() ? name : name + " — " + variant, str(a[2]),
                    nameOr(a[3], "Uncategorised"), str(a[4]), nameOr(a[5], "—"), qty,
                    price == null ? BigDecimal.ZERO.setScale(2) : price, value, status));
        }
        lines.sort(Comparator.comparing((StockLine s) -> s.item().toLowerCase(Locale.ROOT))
                .thenComparing(s -> s.warehouse().toLowerCase(Locale.ROOT)));

        BigDecimal totalValue = BigDecimal.ZERO;
        int low = 0, out = 0;
        Set<String> skus = new LinkedHashSet<>();
        for (StockLine s : lines) {
            totalValue = totalValue.add(s.value());
            if ("red".equals(s.status())) out++; else if ("amber".equals(s.status())) low++;
            skus.add((s.sku().isBlank() ? s.item() : s.sku()).toLowerCase(Locale.ROOT));
        }
        List<ReportDto.Card> cards = List.of(
                card("skus", "Total SKUs", "count", BigDecimal.valueOf(skus.size()), "default"),
                card("value", "Total Stock Value", "money", totalValue, "primary"),
                card("low", "Low Stock", "count", BigDecimal.valueOf(low), "amber"),
                card("out", "Out of Stock", "count", BigDecimal.valueOf(out), "red"));

        List<String> notes = new ArrayList<>(List.of(
                "Current stock snapshot (the date range does not apply). Each row is one stock line per warehouse, same as the Inventory page.",
                "Out of stock = 0 units. Low stock = 1 to 20 units. Low / Out counts are per stock line.",
                "Valued at selling price because no cost price is stored. This is not accounting inventory value."));
        if (noPrice > 0) notes.add(noPrice + " stock line(s) have no selling price and are valued at 0.");

        List<ReportDto.Option> options = opts("item", "Item", "category", "Category", "warehouse", "Warehouse");
        List<ReportDto.Column> columns;
        List<Map<String, Object>> all = new ArrayList<>();
        Map<String, Object> totals;

        if ("item".equals(g)) {
            columns = List.of(col("item", "Item", "left", "text", "default"), col("sku", "SKU", "left", "text", "muted"),
                    col("category", "Category", "left", "text", "muted"), col("warehouse", "Warehouse", "left", "text", "muted"),
                    colQty("stock", "Stock", "unit"), col("price", "Unit Price", "right", "money", "default"),
                    col("value", "Value", "right", "money", "primary"), col("status", "Status", "center", "stock", "default"));
            for (StockLine s : lines) all.add(row("item", s.item(), "sku", s.sku(), "category", s.category(), "warehouse", s.warehouse(),
                    "stock", s.qty(), "unit", s.unit(), "price", s.price(), "value", s.value(), "status", s.status()));
            totals = row("value", totalValue);
        } else {
            Map<String, Agg> m = new LinkedHashMap<>();
            for (StockLine s : lines) {
                Agg a = agg(m, "category".equals(g) ? s.category() : s.warehouse());
                a.count++; a.amount = a.amount.add(s.value());
            }
            List<Agg> list = new ArrayList<>(m.values());
            list.sort((x, y) -> y.amount.compareTo(x.amount));
            columns = List.of(col("label", "category".equals(g) ? "Category" : "Warehouse", "left", "text", "default"),
                    col("count", "Stock lines", "center", "int", "default"), col("value", "Value", "right", "money", "primary"));
            for (Agg a : list) all.add(row("label", a.label, "count", a.count, "value", a.amount));
            totals = row("count", lines.size(), "value", totalValue);
        }
        return report("stock", "Stock Report", null, today, g, options, cards, List.of(), columns, all, totals, notes, page, size);
    }

    // ==================================================================
    // RECEIVABLES / PAYABLES (aging)
    // ==================================================================
    private record Open(String ref, String party, LocalDate date, LocalDate due, BigDecimal amount, BigDecimal paid,
                        BigDecimal balance, int bucket) {}

    private record AgingCfg(String type, String title, String partyLabel, String refLabel, String countLabel,
                            String totalLabel, String partiesLabel, String partyGroup, String balanceTone) {}

    @Override
    public ReportDto.Report receivables(String fromS, String toS, String groupBy, String pageS, String sizeS) {
        Range r = range(fromS, toS);
        String g = group(groupBy, List.of("date", "customer"));
        int page = intParam(pageS, 1, 1, 1_000_000, "page");
        int size = intParam(sizeS, DEFAULT_PAGE_SIZE, 1, MAX_PAGE_SIZE, "size");
        LocalDate asOf = r.to();

        List<Open> items = new ArrayList<>();
        for (Object[] a : repo.openInvoices(asOf, EXCLUDED_OPEN)) {
            BigDecimal total = money(a[4]), paid = money(a[5]);
            BigDecimal bal = total.subtract(paid);
            if (bal.signum() <= 0) continue;
            LocalDate due = (LocalDate) a[2];
            items.add(new Open(str(a[0]), nameOr(a[3], "—"), (LocalDate) a[1], due, total, paid, bal, ageBucket(due, asOf)));
        }
        List<String> notes = new ArrayList<>();
        notes.add("All invoices dated up to " + asOf + " that still have a balance. The From date is not used.");
        if (asOf.isBefore(today())) {
            notes.add("Balances are current: payments recorded after " + asOf + " are already deducted.");
        }
        BigDecimal unapplied = money(repo.unappliedReceipts(asOf));
        if (unapplied.signum() > 0) {
            notes.add("Unapplied receipts (not linked to any invoice): " + IndianCurrency.full(unapplied)
                    + ". They are not deducted from the balances above.");
        }
        return aging(new AgingCfg("receivable", "Receivable Report", "Customer", "Invoice", "Total Invoices",
                "Total Balance", "Customers", "customer", "amber"), items, r, asOf, g, notes, page, size);
    }

    @Override
    public ReportDto.Report payables(String fromS, String toS, String groupBy, String pageS, String sizeS) {
        Range r = range(fromS, toS);
        String g = group(groupBy, List.of("date", "vendor"));
        int page = intParam(pageS, 1, 1, 1_000_000, "page");
        int size = intParam(sizeS, DEFAULT_PAGE_SIZE, 1, MAX_PAGE_SIZE, "size");
        LocalDate asOf = r.to();

        List<Open> items = new ArrayList<>();
        for (Object[] a : repo.openBills(asOf)) {
            double amt = num(a[5]);
            double paid = r2(num(a[6]));
            double bal = Math.max(0.0, r2(amt - paid));
            if (bal <= EPSILON) continue;   // same paid boundary as PurchaseBillServiceImpl
            LocalDate due = (LocalDate) a[3];
            items.add(new Open(str(a[1]), nameOr(a[4], "—"), (LocalDate) a[2], due, dbl(amt), dbl(paid), dbl(bal), ageBucket(due, asOf)));
        }
        List<String> notes = new ArrayList<>();
        notes.add("All vendor bills dated up to " + asOf + " that still have a balance. The From date is not used.");
        if (asOf.isBefore(today())) {
            notes.add("Balances are current: payments recorded after " + asOf + " are already deducted.");
        }
        BigDecimal advances = money(repo.vendorPaymentsApplied(asOf)).subtract(money(repo.vendorPaymentsAllocated(asOf))).max(BigDecimal.ZERO);
        if (advances.signum() > 0) {
            notes.add("Vendor payments not allocated to any bill (advances): " + IndianCurrency.full(advances)
                    + ". They are not deducted from the balances above.");
        }
        return aging(new AgingCfg("payable", "Payable Report", "Vendor", "Bill", "Total Bills",
                "Total Payable", "Vendors", "vendor", "red"), items, r, asOf, g, notes, page, size);
    }

    private ReportDto.Report aging(AgingCfg c, List<Open> items, Range r, LocalDate asOf, String g,
                                   List<String> notes, int page, int size) {
        BigDecimal total = BigDecimal.ZERO, overdue = BigDecimal.ZERO;
        BigDecimal[] bucket = new BigDecimal[5];
        Arrays.fill(bucket, BigDecimal.ZERO);
        Set<String> parties = new LinkedHashSet<>();
        for (Open o : items) {
            total = total.add(o.balance());
            if (o.bucket() > 0) overdue = overdue.add(o.balance());
            bucket[o.bucket()] = bucket[o.bucket()].add(o.balance());
            parties.add(o.party().toLowerCase(Locale.ROOT));
        }
        List<ReportDto.Card> cards = List.of(
                card("count", c.countLabel(), "count", BigDecimal.valueOf(items.size()), "default"),
                card("balance", c.totalLabel(), "money", total, c.balanceTone()),
                card("overdue", "Overdue", "money", overdue, "red"),
                card("parties", c.partiesLabel(), "count", BigDecimal.valueOf(parties.size()), "default"));
        List<ReportDto.Card> breakdown = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            breakdown.add(card(AGE_KEYS[i], AGE_LABELS[i], "money", bucket[i], i == 0 ? "green" : i <= 2 ? "amber" : "red"));
        }
        notes.add("Aging is counted from the due date as of " + asOf + ". Current = not yet due (due today counts as current).");

        List<ReportDto.Option> options = opts("date", "Date", c.partyGroup(), c.partyLabel());
        List<ReportDto.Column> columns;
        List<Map<String, Object>> all = new ArrayList<>();
        Map<String, Object> totals;

        if (g.equals(c.partyGroup())) {
            Map<String, AgeAgg> m = new LinkedHashMap<>();
            for (Open o : items) {
                AgeAgg a = m.computeIfAbsent(o.party().toLowerCase(Locale.ROOT), k -> new AgeAgg(o.party()));
                a.count++;
                a.bucket[o.bucket()] = a.bucket[o.bucket()].add(o.balance());
                a.total = a.total.add(o.balance());
            }
            List<AgeAgg> list = new ArrayList<>(m.values());
            list.sort((x, y) -> y.total.compareTo(x.total));
            columns = List.of(col("label", c.partyLabel(), "left", "text", "default"),
                    col("count", "Open", "center", "int", "default"),
                    col(AGE_KEYS[0], AGE_LABELS[0], "right", "money", "green"),
                    col(AGE_KEYS[1], AGE_LABELS[1], "right", "money", "amber"),
                    col(AGE_KEYS[2], AGE_LABELS[2], "right", "money", "amber"),
                    col(AGE_KEYS[3], AGE_LABELS[3], "right", "money", "red"),
                    col(AGE_KEYS[4], AGE_LABELS[4], "right", "money", "red"),
                    col("balance", "Balance", "right", "money", c.balanceTone()));
            for (AgeAgg a : list) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("label", a.label); row.put("count", a.count);
                for (int i = 0; i < 5; i++) row.put(AGE_KEYS[i], a.bucket[i]);
                row.put("balance", a.total);
                all.add(row);
            }
            Map<String, Object> t = new LinkedHashMap<>();
            t.put("count", items.size());
            for (int i = 0; i < 5; i++) t.put(AGE_KEYS[i], bucket[i]);
            t.put("balance", total);
            totals = t;
        } else {
            columns = List.of(col("party", c.partyLabel(), "left", "text", "default"), col("ref", c.refLabel(), "left", "text", "muted"),
                    col("date", "Date", "left", "date", "muted"), col("due", "Due Date", "left", "date", "muted"),
                    col("amount", "Amount", "right", "money", "default"), col("paid", "Paid", "right", "money", "green"),
                    col("balance", "Balance", "right", "money", c.balanceTone()), col("aging", "Aging", "center", "text", "default"));
            BigDecimal amountSum = BigDecimal.ZERO, paidSum = BigDecimal.ZERO;
            for (Open o : items) {
                amountSum = amountSum.add(o.amount()); paidSum = paidSum.add(o.paid());
                all.add(row("party", o.party(), "ref", o.ref(), "date", o.date(), "due", o.due(), "amount", o.amount(),
                        "paid", o.paid(), "balance", o.balance(), "aging", AGE_LABELS[o.bucket()]));
            }
            totals = row("amount", amountSum, "paid", paidSum, "balance", total);
        }
        return report(c.type(), c.title(), r, asOf, g, options, cards, breakdown, columns, all, totals, notes, page, size);
    }

    private int ageBucket(LocalDate due, LocalDate asOf) {
        if (due == null || !due.isBefore(asOf)) return 0;
        long d = ChronoUnit.DAYS.between(due, asOf);
        return d <= 30 ? 1 : d <= 60 ? 2 : d <= 90 ? 3 : 4;
    }

    // ==================================================================
    // PROJECTS
    // ==================================================================
    @Override
    public ReportDto.Report projects(String fromS, String toS, String pageS, String sizeS) {
        Range r = range(fromS, toS);
        int page = intParam(pageS, 1, 1, 1_000_000, "page");
        int size = intParam(sizeS, DEFAULT_PAGE_SIZE, 1, MAX_PAGE_SIZE, "size");

        BigDecimal budget = BigDecimal.ZERO, revenue = BigDecimal.ZERO, cost = BigDecimal.ZERO, profit = BigDecimal.ZERO;
        List<Map<String, Object>> all = new ArrayList<>();
        int count = 0;
        // Revenue / cost / profit come from ProjectService so they equal the Projects page exactly.
        for (ProjectEntity p : projectRepository.findAll(Sort.by(Sort.Direction.DESC, "projectPrimeId"))) {
            if (!overlaps(p.getStartDate(), p.getEndDate(), r)) continue;
            ProjectResponseDto d = projectService.getProject(p.getProjectStrId());
            BigDecimal b = nz(d.getBudget()), rev = nz(d.getRevenue()), cst = nz(d.getActualCost()), prf = nz(d.getProfit());
            budget = budget.add(b); revenue = revenue.add(rev); cost = cost.add(cst); profit = profit.add(prf);
            count++;
            BigDecimal margin = rev.signum() > 0 ? prf.multiply(HUNDRED).divide(rev, 1, RoundingMode.HALF_UP) : null;
            all.add(row("project", str(d.getName()),
                    "projectSub", str(d.getCode()) + " · " + str(d.getCustomer()) + " · " + titleCase(d.getStatus()),
                    "budget", b, "revenue", rev, "cost", cst, "profit", prf, "margin", margin));
        }
        List<ReportDto.Card> cards = List.of(
                card("projects", "Total Projects", "count", BigDecimal.valueOf(count), "default"),
                card("revenue", "Total Revenue", "money", revenue, "primary"),
                card("cost", "Total Cost", "money", cost, "red"),
                card("profit", "Net Profit", "money", profit, "signed"));
        List<ReportDto.Column> columns = List.of(colSub("project", "Project", "projectSub"),
                col("budget", "Budget", "right", "money", "default"), col("revenue", "Revenue", "right", "money", "primary"),
                col("cost", "Cost", "right", "money", "red"), col("profit", "Profit", "right", "money", "signed"),
                col("margin", "Margin", "right", "percent", "signed"));
        List<String> notes = List.of(
                "Revenue (linked invoices), cost (linked bills + approved expenses) and profit are all-time figures, the same as the Projects page.",
                "A project is listed when its start-end dates overlap the selected range. Margin is blank when a project has no revenue yet.");
        return report("project", "Project Report", r, null, "date", List.of(), cards, List.of(), columns, all,
                row("budget", budget, "revenue", revenue, "cost", cost, "profit", profit), notes, page, size);
    }

    private boolean overlaps(LocalDate start, LocalDate end, Range r) {
        return (start == null || !start.isAfter(r.to())) && (end == null || !end.isBefore(r.from()));
    }

    // ==================================================================
    // shared report plumbing
    // ==================================================================
    private ReportDto.Report itemReport(String type, String title, Range r, String g, String labelHeader,
                                        Map<String, ItemAgg> m, List<ReportDto.Card> cards, List<String> notes,
                                        List<ReportDto.Option> options, int page, int size) {
        List<ItemAgg> list = new ArrayList<>(m.values());
        list.sort((x, y) -> y.total().compareTo(x.total()));
        BigDecimal taxable = BigDecimal.ZERO, tax = BigDecimal.ZERO;
        List<Map<String, Object>> all = new ArrayList<>();
        for (ItemAgg a : list) {
            taxable = taxable.add(a.taxable); tax = tax.add(a.tax);
            all.add(row("label", a.label, "qty", a.qty, "taxable", a.taxable, "tax", a.tax, "total", a.total()));
        }
        List<ReportDto.Column> columns = List.of(col("label", labelHeader, "left", "text", "default"),
                col("qty", "Qty", "right", "qty", "default"), col("taxable", "Taxable Value", "right", "money", "default"),
                col("tax", "Tax", "right", "money", "muted"), col("total", "Total", "right", "money", "primary"));
        return report(type, title, r, null, g, options, cards, List.of(), columns, all,
                row("taxable", taxable, "tax", tax, "total", taxable.add(tax)), notes, page, size);
    }

    private ReportDto.Report report(String type, String title, Range r, LocalDate asOf, String g,
                                    List<ReportDto.Option> options, List<ReportDto.Card> cards,
                                    List<ReportDto.Card> breakdown, List<ReportDto.Column> columns,
                                    List<Map<String, Object>> all, Map<String, Object> totals,
                                    List<String> notes, int page, int size) {
        int total = all.size();
        int fromIdx = Math.min((page - 1) * size, total);
        List<Map<String, Object>> content = new ArrayList<>(all.subList(fromIdx, Math.min(fromIdx + size, total)));
        return ReportDto.Report.builder()
                .meta(ReportDto.Meta.builder().type(type).title(title)
                        .from(r == null ? null : r.from()).to(r == null ? null : r.to())
                        .asOf(asOf).groupBy(g).groupByOptions(options).build())
                .cards(cards).breakdown(breakdown).columns(columns).rows(content).totals(totals)
                .pagination(ReportDto.Pagination.builder().page(page).size(size).totalItems(total)
                        .totalPages(Math.max(1, (int) Math.ceil(total / (double) size))).build())
                .notes(notes)
                .build();
    }

    private static final class Agg {
        final String label; int count;
        BigDecimal amount = BigDecimal.ZERO, tax = BigDecimal.ZERO, paid = BigDecimal.ZERO, due = BigDecimal.ZERO;
        Agg(String label) { this.label = label; }
    }
    private static final class ItemAgg {
        final String label;
        BigDecimal qty = BigDecimal.ZERO, taxable = BigDecimal.ZERO, tax = BigDecimal.ZERO;
        ItemAgg(String label) { this.label = label; }
        BigDecimal total() { return taxable.add(tax); }
    }
    private static final class AgeAgg {
        final String label; int count; BigDecimal total = BigDecimal.ZERO;
        final BigDecimal[] bucket = new BigDecimal[5];
        AgeAgg(String label) { this.label = label; Arrays.fill(bucket, BigDecimal.ZERO); }
    }
    private Agg agg(Map<String, Agg> m, String name) {
        return m.computeIfAbsent(name.trim().toLowerCase(Locale.ROOT), k -> new Agg(name.trim()));
    }
    private ItemAgg itemAgg(Map<String, ItemAgg> m, String name) {
        return m.computeIfAbsent(name.trim().toLowerCase(Locale.ROOT), k -> new ItemAgg(name.trim()));
    }

    // ---------- params ----------
    private record Range(LocalDate from, LocalDate to) {}

    private Range range(String fromS, String toS) {
        LocalDate to = isBlank(toS) ? today() : parseDate(toS, "To");
        LocalDate from = isBlank(fromS) ? to.minusDays(DEFAULT_RANGE_DAYS) : parseDate(fromS, "From");
        if (from.isAfter(to)) throw new IllegalArgumentException("From date cannot be after To date.");
        if (ChronoUnit.DAYS.between(from, to) > MAX_RANGE_DAYS) {
            throw new IllegalArgumentException("Date range is too long. Please choose 5 years or less.");
        }
        return new Range(from, to);
    }

    private LocalDate parseDate(String s, String label) {
        try {
            return LocalDate.parse(s.trim());
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid " + label + " date '" + s + "'. Use yyyy-MM-dd.");
        }
    }

    private int intParam(String s, int def, int min, int max, String name) {
        if (isBlank(s)) return def;
        try {
            int v = Integer.parseInt(s.trim());
            if (v < min || v > max) throw new IllegalArgumentException("'" + name + "' must be between " + min + " and " + max + ".");
            return v;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("'" + name + "' must be a number.");
        }
    }

    private String group(String raw, List<String> allowed) {
        if (isBlank(raw)) return allowed.get(0);
        String g = raw.trim().toLowerCase(Locale.ROOT);
        if (!allowed.contains(g)) {
            throw new IllegalArgumentException("Invalid groupBy '" + raw + "'. Allowed: " + String.join(", ", allowed) + ".");
        }
        return g;
    }

    // ---------- small builders / converters ----------
    private LocalDate today() { return LocalDate.now(BUSINESS_ZONE); }

    private ReportDto.Card card(String key, String label, String kind, BigDecimal value, String tone) {
        return ReportDto.Card.builder().key(key).label(label).kind(kind).value(value).tone(tone).build();
    }
    private ReportDto.Column col(String key, String label, String align, String type, String tone) {
        return ReportDto.Column.builder().key(key).label(label).align(align).type(type).tone(tone).build();
    }
    private ReportDto.Column colSub(String key, String label, String subKey) {
        return ReportDto.Column.builder().key(key).label(label).align("left").type("text").tone("default").subKey(subKey).build();
    }
    private ReportDto.Column colQty(String key, String label, String unitKey) {
        return ReportDto.Column.builder().key(key).label(label).align("center").type("qty").tone("default").unitKey(unitKey).build();
    }
    private List<ReportDto.Option> opts(String... pairs) {
        List<ReportDto.Option> out = new ArrayList<>();
        for (int i = 0; i + 1 < pairs.length; i += 2) out.add(ReportDto.Option.builder().value(pairs[i]).label(pairs[i + 1]).build());
        return out;
    }
    private static Map<String, Object> row(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i + 1 < kv.length; i += 2) m.put((String) kv[i], kv[i + 1]);
        return m;
    }

    private BigDecimal dec(Object o) {
        if (o == null) return BigDecimal.ZERO;
        if (o instanceof BigDecimal) return (BigDecimal) o;
        if (o instanceof Number) return BigDecimal.valueOf(((Number) o).doubleValue());
        return new BigDecimal(o.toString());
    }
    private BigDecimal money(Object o) { return dec(o).setScale(2, RoundingMode.HALF_UP); }
    private BigDecimal nz(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }
    private BigDecimal dbl(double d) { return BigDecimal.valueOf(d).setScale(2, RoundingMode.HALF_UP); }
    private double num(Object o) { return o == null ? 0.0 : ((Number) o).doubleValue(); }
    private double r2(double n) { return Math.round(n * 100.0) / 100.0; }   // same rounding as PurchaseBillServiceImpl
    private String str(Object o) { return o == null ? "" : String.valueOf(o); }
    private String nameOr(Object o, String fallback) { String s = str(o).trim(); return s.isEmpty() ? fallback : s; }
    private boolean isBlank(String s) { return s == null || s.isBlank(); }
    private String titleCase(String s) { return isBlank(s) ? "" : s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1).toLowerCase(Locale.ROOT); }


    // ==================================================================
    // GST (monthly summary)
    // ==================================================================
    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);

    private record InvParts(BigDecimal total, BigDecimal taxable, BigDecimal tax) {}

    // Same arithmetic as InvoiceServiceImpl.applyTotals / toSale(): discount, then tax on the discounted subtotal.
    private InvParts invParts(Object[] a) {
        BigDecimal total = money(a[4]);
        BigDecimal sub = dec(a[6]), disc = dec(a[7]), taxPct = dec(a[8]);
        BigDecimal fx = a[9] == null ? BigDecimal.ONE : dec(a[9]);
        BigDecimal afterDisc = sub.subtract(sub.multiply(disc).divide(HUNDRED, 2, RoundingMode.HALF_UP));
        BigDecimal taxDoc = afterDisc.multiply(taxPct).divide(HUNDRED, 2, RoundingMode.HALF_UP);
        return new InvParts(total, afterDisc.multiply(fx).setScale(2, RoundingMode.HALF_UP),
                taxDoc.multiply(fx).setScale(2, RoundingMode.HALF_UP));
    }

    private BigDecimal[] zeros(int n) {
        BigDecimal[] z = new BigDecimal[n];
        Arrays.fill(z, BigDecimal.ZERO.setScale(2));
        return z;
    }

    @Override
    public ReportDto.Report gst(String fromS, String toS) {
        Range r = range(fromS, toS);
        List<YearMonth> months = new ArrayList<>();
        for (YearMonth m = YearMonth.from(r.from()); !m.isAfter(YearMonth.from(r.to())); m = m.plusMonths(1)) months.add(m);
        Map<YearMonth, BigDecimal[]> acc = new LinkedHashMap<>();   // [taxable sales, output tax, taxable purchase, input tax]
        for (YearMonth m : months) acc.put(m, zeros(4));

        for (Object[] a : repo.salesRows(r.from(), r.to(), EXCLUDED_SALES)) {
            InvParts p = invParts(a);
            BigDecimal[] t = acc.get(YearMonth.from((LocalDate) a[1]));
            t[0] = t[0].add(p.taxable());
            t[1] = t[1].add(p.tax());
        }

        Map<Long, YearMonth> billMonth = new HashMap<>();
        Map<Long, Double> billAmount = new HashMap<>();
        for (Object[] a : repo.billRows(r.from(), r.to())) {
            long id = ((Number) a[0]).longValue();
            billMonth.put(id, YearMonth.from((LocalDate) a[2]));
            billAmount.put(id, num(a[5]));
        }
        // Per-line maths exactly like PurchaseBillServiceImpl.line(): round2(qty*rate), then round2(amount*gst%/100).
        Set<Long> withItems = new HashSet<>();
        for (Object[] a : repo.billItemLines(r.from(), r.to())) {
            long id = ((Number) a[0]).longValue();
            YearMonth ym = billMonth.get(id);
            if (ym == null) continue;
            int qty = a[2] == null ? 0 : ((Number) a[2]).intValue();
            double lineAmt = r2(qty * num(a[3]));
            double lineGst = r2(lineAmt * num(a[4]) / 100.0);
            BigDecimal[] t = acc.get(ym);
            t[2] = t[2].add(dbl(lineAmt));
            t[3] = t[3].add(dbl(lineGst));
            withItems.add(id);
        }
        int noItems = 0;
        BigDecimal noItemsAmount = BigDecimal.ZERO;
        for (Map.Entry<Long, Double> e : billAmount.entrySet()) {
            if (!withItems.contains(e.getKey())) { noItems++; noItemsAmount = noItemsAmount.add(dbl(e.getValue())); }
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        BigDecimal sales = BigDecimal.ZERO, out = BigDecimal.ZERO, purchase = BigDecimal.ZERO, in = BigDecimal.ZERO;
        boolean anyPartial = false;
        for (YearMonth m : months) {
            BigDecimal[] t = acc.get(m);
            boolean partial = m.atDay(1).isBefore(r.from()) || m.atEndOfMonth().isAfter(r.to());
            anyPartial = anyPartial || partial;
            rows.add(row("period", m.format(MONTH_FMT) + (partial ? " (partial)" : ""), "sales", t[0], "outTax", t[1],
                    "purchase", t[2], "inTax", t[3], "net", t[1].subtract(t[3])));
            sales = sales.add(t[0]); out = out.add(t[1]); purchase = purchase.add(t[2]); in = in.add(t[3]);
        }
        BigDecimal net = out.subtract(in);
        boolean credit = net.signum() < 0;

        List<ReportDto.Card> cards = List.of(
                card("out", "Output Tax", "money", out, "amber"),
                card("in", "Input Tax", "money", in, "blue"),
                card("net", credit ? "Net GST Credit" : "Net GST Payable", "money", net.abs(), credit ? "green" : "amber"),
                card("months", "Months in range", "count", BigDecimal.valueOf(months.size()), "default"));
        List<ReportDto.Column> columns = List.of(
                col("period", "Period", "left", "text", "default"),
                col("sales", "Taxable Sales", "right", "money", "default"),
                col("outTax", "Output Tax", "right", "money", "amber"),
                col("purchase", "Taxable Purchase", "right", "money", "default"),
                col("inTax", "Input Tax", "right", "money", "blue"),
                col("net", "Net GST", "right", "money", "signedInverse"));

        List<String> notes = new ArrayList<>(List.of(
                "Output GST: non-cancelled invoices by invoice date. Tax is charged on (subtotal - discount) at the invoice GST rate, converted to rupees. Shipping and other charges are not taxed.",
                "Input GST: vendor-bill line items by bill date, GST per line. Taxable Purchase is quantity x rate before GST.",
                "Net GST = Output - Input. A negative (green) amount is an input credit for that month; credits are not carried forward or set off here.",
                "GST on expenses is not included in Input GST (same as the Dashboard).",
                "This is a management summary, not a GST return."));
        if (anyPartial) notes.add("Months marked (partial) are only partly inside the selected dates.");
        if (noItems > 0) {
            notes.add(noItems + " bill(s) worth " + IndianCurrency.full(noItemsAmount)
                    + " have no line items, so their GST is not included in Input GST.");
        }
        return report("tax", "GST Report", r, null, "month", List.of(), cards, List.of(), columns, rows,
                row("sales", sales, "outTax", out, "purchase", purchase, "inTax", in, "net", net), notes, 1, MAX_PAGE_SIZE);
    }

    // ==================================================================
    // ACCOUNTING (management P&L + working-capital position)
    // ==================================================================
    private ReportDto.StatementLine sl(String label, BigDecimal amount, String kind, String tone, int indent) {
        return ReportDto.StatementLine.builder().label(label).amount(amount).kind(kind).tone(tone).indent(indent).build();
    }

    private BigDecimal receivableTotal(LocalDate asOf) {
        BigDecimal t = BigDecimal.ZERO;
        for (Object[] a : repo.openInvoices(asOf, EXCLUDED_OPEN)) {
            BigDecimal bal = money(a[4]).subtract(money(a[5]));
            if (bal.signum() > 0) t = t.add(bal);
        }
        return t;
    }

    private BigDecimal payableTotal(LocalDate asOf) {
        BigDecimal t = BigDecimal.ZERO;
        for (Object[] a : repo.openBills(asOf)) {
            double paid = r2(num(a[6]));
            double bal = Math.max(0.0, r2(num(a[5]) - paid));
            if (bal > EPSILON) t = t.add(dbl(bal));
        }
        return t;
    }

    private BigDecimal inventoryValue() {
        BigDecimal v = BigDecimal.ZERO;
        for (Object[] a : repo.stockRows()) {
            if (a[7] == null) continue;
            v = v.add(dec(a[6]).setScale(2, RoundingMode.HALF_UP).multiply(dec(a[7]).setScale(2, RoundingMode.HALF_UP))
                    .setScale(2, RoundingMode.HALF_UP));
        }
        return v;
    }

    @Override
    public ReportDto.Report accounting(String fromS, String toS) {
        Range r = range(fromS, toS);
        LocalDate asOf = r.to();

        // ----- income, ex-GST (gross invoice total minus output GST) -----
        BigDecimal grossInv = BigDecimal.ZERO, outGst = BigDecimal.ZERO, taxable = BigDecimal.ZERO;
        for (Object[] a : repo.salesRows(r.from(), r.to(), EXCLUDED_SALES)) {
            InvParts p = invParts(a);
            grossInv = grossInv.add(p.total());
            outGst = outGst.add(p.tax());
            taxable = taxable.add(p.taxable());
        }
        BigDecimal revenue = grossInv.subtract(outGst);
        BigDecimal otherCharges = revenue.subtract(taxable);

        // ----- purchases, ex-GST (bill amount minus the GST on its line items) -----
        BigDecimal billsGross = BigDecimal.ZERO, billsGst = BigDecimal.ZERO;
        for (Object[] a : repo.billRows(r.from(), r.to())) billsGross = billsGross.add(dbl(num(a[5])));
        for (Object[] a : repo.billItemLines(r.from(), r.to())) {
            int qty = a[2] == null ? 0 : ((Number) a[2]).intValue();
            double lineAmt = r2(qty * num(a[3]));
            billsGst = billsGst.add(dbl(r2(lineAmt * num(a[4]) / 100.0)));
        }
        BigDecimal billsEx = billsGross.subtract(billsGst);

        // ----- operating expenses by category (approved, tax included) -----
        List<Object[]> cats = new ArrayList<>(repo.expensesByCategory(r.from(), r.to()));
        cats.sort((x, y) -> money(y[2]).compareTo(money(x[2])));
        BigDecimal expTotal = BigDecimal.ZERO;
        for (Object[] c : cats) expTotal = expTotal.add(money(c[2]));
        BigDecimal totalExpenses = billsEx.add(expTotal);
        BigDecimal net = revenue.subtract(totalExpenses);

        List<ReportDto.StatementLine> pl = new ArrayList<>();
        pl.add(sl("INCOME", null, "header", "default", 0));
        pl.add(sl("Sales (net of discount)", taxable, "line", "default", 1));
        pl.add(sl("Shipping, other charges & rounding", otherCharges, "line", "default", 1));
        pl.add(sl("Total Income (ex-GST)", revenue, "subtotal", "green", 0));
        pl.add(sl("EXPENSES", null, "header", "default", 0));
        pl.add(sl("Purchases - vendor bills (ex-GST)", billsEx, "line", "default", 1));
        BigDecimal others = BigDecimal.ZERO;
        for (int i = 0; i < cats.size(); i++) {
            BigDecimal amt = money(cats.get(i)[2]);
            if (i < 10) pl.add(sl(nameOr(cats.get(i)[1], "Uncategorised"), amt, "line", "default", 1));
            else others = others.add(amt);
        }
        if (others.signum() > 0) pl.add(sl("Other expenses", others, "line", "default", 1));
        pl.add(sl("Total Expenses", totalExpenses, "subtotal", "red", 0));
        pl.add(sl("NET PROFIT / (LOSS)", net, "grand", "signed", 0));

        // ----- working-capital position as of the To date -----
        BigDecimal receivables = receivableTotal(asOf);
        BigDecimal inventory = inventoryValue();
        BigDecimal cash = openingBalance.add(money(repo.receiptsUpTo(asOf)))
                .subtract(money(repo.billPaymentsUpTo(asOf)))
                .subtract(money(repo.approvedExpensesUpTo(asOf)))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal payables = payableTotal(asOf);
        BigDecimal assets = receivables.add(inventory).add(cash);
        BigDecimal unapplied = money(repo.unappliedReceipts(asOf));
        BigDecimal advances = money(repo.vendorPaymentsApplied(asOf)).subtract(money(repo.vendorPaymentsAllocated(asOf))).max(BigDecimal.ZERO);
        BigDecimal gstNet = outGst.subtract(billsGst);

        List<ReportDto.StatementLine> wc = new ArrayList<>();
        wc.add(sl("CURRENT ASSETS", null, "header", "default", 0));
        wc.add(sl("Receivables (open invoices)", receivables, "line", "default", 1));
        wc.add(sl("Inventory (at selling price)", inventory, "line", "default", 1));
        wc.add(sl("Cash (estimate)", cash, "line", "default", 1));
        wc.add(sl("Total Current Assets", assets, "subtotal", "primary", 0));
        wc.add(sl("CURRENT LIABILITIES", null, "header", "default", 0));
        wc.add(sl("Payables (open bills)", payables, "line", "default", 1));
        wc.add(sl("Total Current Liabilities", payables, "subtotal", "red", 0));
        wc.add(sl("NET WORKING CAPITAL", assets.subtract(payables), "grand", "signed", 0));
        wc.add(sl("MEMO (not included above)", null, "header", "default", 0));
        wc.add(sl("Unapplied customer receipts", unapplied, "memo", "muted", 1));
        wc.add(sl("Vendor advances (not allocated to bills)", advances, "memo", "muted", 1));
        wc.add(sl("Net GST for the selected dates (before payments made)", gstNet, "memo", "muted", 1));

        // ----- reconciliation with the Dashboard (which includes GST on both sides) -----
        BigDecimal dashNet = grossInv.subtract(billsGross).subtract(expTotal);
        List<ReportDto.StatementLine> rec = new ArrayList<>();
        rec.add(sl("Income less expenses, GST included (as on the Dashboard)", dashNet, "line", "signed", 0));
        rec.add(sl("less: Net GST for the selected dates (output - input)", gstNet, "line", "default", 0));
        rec.add(sl("Net profit (this report)", dashNet.subtract(gstNet), "grand", "signed", 0));

        List<ReportDto.Statement> statements = List.of(
                ReportDto.Statement.builder().key("pl").title("Profit & Loss Summary")
                        .subtitle(r.from() + " to " + r.to() + " - accrual basis, ex-GST").lines(pl).build(),
                ReportDto.Statement.builder().key("wc").title("Working Capital Position")
                        .subtitle("As of " + asOf).lines(wc).build(),
                ReportDto.Statement.builder().key("rec").title("Reconciliation with Dashboard")
                        .subtitle("Dashboard income and expense include GST; this report excludes it").lines(rec).build());

        List<String> notes = List.of(
                "Management view built from invoices, vendor bills and approved expenses. There is no ledger behind it, so it is not a statutory P&L or balance sheet.",
                "Income and purchases are shown without GST. Expense tax is treated as cost because it is not claimed as input credit here. Income is not split into product and service.",
                "Stock purchases count as purchases when billed: there is no opening or closing stock adjustment, depreciation, payroll (unless logged as expenses), loans or capital.",
                "Cash is an estimate: opening balance + customer receipts - bill payments - approved expenses, up to the To date. Inventory is a current snapshot valued at selling price.",
                "Receivable and payable balances are current balances; payments recorded after the To date are already deducted.");
        return ReportDto.Report.builder()
                .meta(ReportDto.Meta.builder().type("accounting").title("Accounting Report").from(r.from()).to(r.to())
                        .groupBy("date").groupByOptions(List.of()).build())
                .cards(List.of()).breakdown(List.of()).columns(List.of()).rows(List.of()).totals(Map.of())
                .statements(statements).notes(notes)
                .build();
    }



}