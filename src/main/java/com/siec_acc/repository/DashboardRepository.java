package com.siec_acc.repository;

import com.siec_acc.enum_status.InvoiceStatus;   // adjust package if different
import jakarta.persistence.EntityManager;   // javax.persistence if you are on Spring Boot 2
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * Read-only aggregate queries for the Dashboard. Each call is its own short read transaction, so a failure in
 * one widget can never poison another.
 */
@Repository
@Transactional(readOnly = true)
public class DashboardRepository {

    @PersistenceContext
    private EntityManager em;

    // Invoice total in INR; falls back to the document-currency total (same rule as CustomerPaymentServiceImpl).
    private static final String INV_TOTAL = "coalesce(i.invoiceTotalAmountInr, i.invoiceTotalAmount)";

    // Approved expense incl. its tax %, rounded per row like ProjectServiceImpl.loadExpenses.
    private static final String EXP_GROSS = "(e.expenseAmount + round(e.expenseAmount * coalesce(e.expenseTax, 0) / 100, 2))";

    // ---------------------------------------------------------------- sales / purchases
    public Object sumSales(LocalDate from, LocalDate to, Collection<InvoiceStatus> excluded) {
        return em.createQuery("select coalesce(sum(" + INV_TOTAL + "), 0) from InvoiceEntity i "
                        + "where i.invoiceDate between :from and :to and i.invoiceStatus not in (:excluded)")
                .setParameter("from", from).setParameter("to", to).setParameter("excluded", excluded)
                .getSingleResult();
    }

    /** rows: [year, month, sum] */
    public List<Object[]> monthlySales(LocalDate from, LocalDate to, Collection<InvoiceStatus> excluded) {
        return em.createQuery("select year(i.invoiceDate), month(i.invoiceDate), coalesce(sum(" + INV_TOTAL + "), 0) "
                        + "from InvoiceEntity i where i.invoiceDate between :from and :to "
                        + "and i.invoiceStatus not in (:excluded) "
                        + "group by year(i.invoiceDate), month(i.invoiceDate)", Object[].class)
                .setParameter("from", from).setParameter("to", to).setParameter("excluded", excluded)
                .getResultList();
    }

    public Object sumPurchases(LocalDate from, LocalDate to) {
        return em.createQuery("select coalesce(sum(b.amount), 0) from PurchaseBillEntity b "
                        + "where b.billDate between :from and :to")
                .setParameter("from", from).setParameter("to", to)
                .getSingleResult();
    }

    /** rows: [year, month, sum] */
    public List<Object[]> monthlyPurchases(LocalDate from, LocalDate to) {
        return em.createQuery("select year(b.billDate), month(b.billDate), coalesce(sum(b.amount), 0) "
                        + "from PurchaseBillEntity b where b.billDate between :from and :to "
                        + "group by year(b.billDate), month(b.billDate)", Object[].class)
                .setParameter("from", from).setParameter("to", to)
                .getResultList();
    }

    /** Approved expenses only. rows: [year, month, categoryKey(lower/trim), categoryLabel, sum] */
    public List<Object[]> monthlyExpenses(LocalDate from, LocalDate to) {
        return em.createQuery("select year(e.expenseDate), month(e.expenseDate), lower(trim(e.expenseCategory)), "
                        + "min(trim(e.expenseCategory)), sum" + EXP_GROSS.replace("(e.", "(e.") + " "
                        + "from ExpenseEntity e where lower(e.expenseStatus) = 'approved' "
                        + "and e.expenseDate between :from and :to "
                        + "group by year(e.expenseDate), month(e.expenseDate), lower(trim(e.expenseCategory))", Object[].class)
                .setParameter("from", from).setParameter("to", to)
                .getResultList();
    }

    // ---------------------------------------------------------------- receivables / payables
    /** Unpaid invoices only. rows: [invoiceStrId, customerName, dueDate, totalInr, paid] */
    public List<Object[]> openInvoices(Collection<InvoiceStatus> excluded) {
        return em.createQuery("select i.invoiceStrId, i.invoiceCustomerName, i.invoiceDueDate, " + INV_TOTAL
                        + ", coalesce(i.invoicePaidAmount, 0) from InvoiceEntity i "
                        + "where i.invoiceStatus not in (:excluded) "
                        + "and (" + INV_TOTAL + " - coalesce(i.invoicePaidAmount, 0)) > 0", Object[].class)
                .setParameter("excluded", excluded)
                .getResultList();
    }

    /** Bills with an outstanding balance. rows: [pbNumber, vendorName, dueDate, amount, paidSum] */
    public List<Object[]> openBills() {
        return em.createQuery("select b.pbNumber, b.vendorName, b.dueDate, b.amount, coalesce(sum(p.amount), 0) "
                        + "from PurchaseBillEntity b left join b.payments p "
                        + "group by b.pbPrimeId, b.pbNumber, b.vendorName, b.dueDate, b.amount "
                        + "having b.amount - coalesce(sum(p.amount), 0) > 0.005", Object[].class)
                .getResultList();
    }

    // ---------------------------------------------------------------- GST
    /** rows: [subTotal, discountPercent, taxPercent, exchangeRate] */
    public List<Object[]> invoiceTaxRows(LocalDate from, LocalDate to, Collection<InvoiceStatus> excluded) {
        return em.createQuery("select i.invoiceSubTotal, i.invoiceDiscountPercent, i.invoiceTaxPercent, i.invoiceExchangeRate "
                        + "from InvoiceEntity i where i.invoiceDate between :from and :to "
                        + "and i.invoiceStatus not in (:excluded)", Object[].class)
                .setParameter("from", from).setParameter("to", to).setParameter("excluded", excluded)
                .getResultList();
    }

    /** rows: [qty, rate, gstPct] for items of bills dated in range */
    public List<Object[]> billItemGstRows(LocalDate from, LocalDate to) {
        return em.createQuery("select it.qty, it.rate, it.gstPct from PurchaseBillItemEntity it "
                        + "join it.purchaseBill b where b.billDate between :from and :to", Object[].class)
                .setParameter("from", from).setParameter("to", to)
                .getResultList();
    }

    // ---------------------------------------------------------------- cash position (all time)
    public Object sumCustomerReceipts() {
        return em.createQuery("select coalesce(sum(p.amount), 0) from CustomerPaymentEntity p "
                + "where p.status is null or upper(p.status) = 'RECEIVED'").getSingleResult();
    }

    /** Every payment row on a bill (covers both the bill screen and the vendor-payment screen). */
    public Object sumBillPayments() {
        return em.createQuery("select coalesce(sum(p.amount), 0) from PurchaseBillPaymentEntity p").getSingleResult();
    }

    public Object sumApprovedExpensesGross() {
        return em.createQuery("select coalesce(sum" + EXP_GROSS + ", 0) from ExpenseEntity e "
                + "where lower(e.expenseStatus) = 'approved'").getSingleResult();
    }

    // ---------------------------------------------------------------- projects
    public List<String> activeProjectIds(int limit) {
        return em.createQuery("select p.projectStrId from ProjectEntity p "
                        + "where lower(p.status) = 'active' order by p.projectPrimeId desc", String.class)
                .setMaxResults(limit)
                .getResultList();
    }

    // ---------------------------------------------------------------- alerts
    /** Total stock across warehouses. rows: [productName, variantName, totalStock, lastUpdated] */
    public List<Object[]> lowStock(BigDecimal threshold, int limit) {
        return em.createQuery("select coalesce(vp.productName, p.productName), v.variantName, sum(i.productStock), "
                        + "max(i.inventoryUpdatedAt) "
                        + "from InventoryEntity i left join i.product p left join i.variant v left join v.product vp "
                        + "group by p.productPrimeId, v.variantPrimeId, p.productName, vp.productName, v.variantName "
                        + "having sum(i.productStock) <= :threshold order by sum(i.productStock) asc", Object[].class)
                .setParameter("threshold", threshold)
                .setMaxResults(limit)
                .getResultList();
    }

    /** rows: [poNumber, vendorName, totalAmount, createdAt] */
    public List<Object[]> purchaseOrdersByStatus(String statusLower, int limit) {
        return em.createQuery("select po.poNumber, po.vendorName, po.totalAmount, po.createdAt "
                        + "from PurchaseOrderEntity po where lower(po.status) = :status order by po.createdAt desc", Object[].class)
                .setParameter("status", statusLower)
                .setMaxResults(limit)
                .getResultList();
    }

    /** rows: [paymentId, amount, invoiceReference, createdAt, clientCompanyName, clientName] */
    public List<Object[]> recentReceipts(LocalDateTime since, int limit) {
        return em.createQuery("select p.paymentId, p.amount, p.invoiceReference, p.createdAt, c.clientCompanyName, c.clientName "
                        + "from CustomerPaymentEntity p left join ClientEntity c on c.clientStrId = p.clientStrId "
                        + "where (p.status is null or upper(p.status) = 'RECEIVED') and p.createdAt >= :since "
                        + "order by p.createdAt desc", Object[].class)
                .setParameter("since", since)
                .setMaxResults(limit)
                .getResultList();
    }
}