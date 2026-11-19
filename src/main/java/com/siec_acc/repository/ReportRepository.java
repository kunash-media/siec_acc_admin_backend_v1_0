package com.siec_acc.repository;

import com.siec_acc.enum_status.InvoiceStatus;   // same package as in DashboardRepository
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Repository
@Transactional(readOnly = true)
public class ReportRepository {

    @PersistenceContext
    private EntityManager em;

    private static final String INV_TOTAL = "coalesce(i.invoiceTotalAmountInr, i.invoiceTotalAmount)";

    private static final String BILL_SELECT =
            "select b.pbPrimeId, b.pbNumber, b.billDate, b.dueDate, b.vendorName, b.amount, coalesce(sum(p.amount), 0) "
                    + "from PurchaseBillEntity b left join b.payments p ";
    private static final String BILL_GROUP =
            " group by b.pbPrimeId, b.pbNumber, b.billDate, b.dueDate, b.vendorName, b.amount ";

    // ---------------------------------------------------------------- sales
    /** [0]strId [1]date [2]due [3]customer [4]totalInr [5]paid [6]subTotal [7]discount% [8]tax% [9]fx */
    public List<Object[]> salesRows(LocalDate from, LocalDate to, Collection<InvoiceStatus> excluded) {
        return em.createQuery("select i.invoiceStrId, i.invoiceDate, i.invoiceDueDate, i.invoiceCustomerName, " + INV_TOTAL
                        + ", coalesce(i.invoicePaidAmount, 0), i.invoiceSubTotal, i.invoiceDiscountPercent, "
                        + "i.invoiceTaxPercent, i.invoiceExchangeRate "
                        + "from InvoiceEntity i where i.invoiceDate between :from and :to "
                        + "and i.invoiceStatus not in (:excluded) "
                        + "order by i.invoiceDate asc, i.invoicePrimeId asc", Object[].class)
                .setParameter("from", from).setParameter("to", to).setParameter("excluded", excluded)
                .getResultList();
    }

    /** [0]itemName [1]qty [2]lineAmount [3]discount% [4]tax% [5]fx [6]category */
    public List<Object[]> salesItemRows(LocalDate from, LocalDate to, Collection<InvoiceStatus> excluded) {
        return em.createQuery("select it.invoiceItemName, it.invoiceItemQty, it.invoiceItemAmount, "
                        + "inv.invoiceDiscountPercent, inv.invoiceTaxPercent, inv.invoiceExchangeRate, "
                        + "coalesce(v.variantCategory, vp.productCategory, p.productCategory) "
                        + "from InvoiceItemEntity it join it.invoice inv "
                        + "left join ProductEntity p on p.productPrimeId = it.invoiceItemProductId "
                        + "left join VariantEntity v on v.variantPrimeId = it.invoiceItemVariantId "
                        + "left join v.product vp "
                        + "where inv.invoiceDate between :from and :to and inv.invoiceStatus not in (:excluded)", Object[].class)
                .setParameter("from", from).setParameter("to", to).setParameter("excluded", excluded)
                .getResultList();
    }

    // ---------------------------------------------------------------- purchases
    /** [0]id [1]pbNumber [2]billDate [3]dueDate [4]vendor [5]amount [6]paidSum */
    public List<Object[]> billRows(LocalDate from, LocalDate to) {
        return em.createQuery(BILL_SELECT + "where b.billDate between :from and :to" + BILL_GROUP
                        + "order by b.billDate asc, b.pbPrimeId asc", Object[].class)
                .setParameter("from", from).setParameter("to", to)
                .getResultList();
    }

    /** [0]billId [1]itemName [2]qty [3]rate [4]gstPct */
    public List<Object[]> billItemLines(LocalDate from, LocalDate to) {
        return em.createQuery("select b.pbPrimeId, it.name, it.qty, it.rate, it.gstPct "
                        + "from PurchaseBillItemEntity it join it.purchaseBill b "
                        + "where b.billDate between :from and :to", Object[].class)
                .setParameter("from", from).setParameter("to", to)
                .getResultList();
    }

    // ---------------------------------------------------------------- receivables / payables
    /** Invoices dated up to asOf that still have a balance. [0]strId [1]date [2]due [3]customer [4]totalInr [5]paid */
    public List<Object[]> openInvoices(LocalDate asOf, Collection<InvoiceStatus> excluded) {
        return em.createQuery("select i.invoiceStrId, i.invoiceDate, i.invoiceDueDate, i.invoiceCustomerName, " + INV_TOTAL
                        + ", coalesce(i.invoicePaidAmount, 0) from InvoiceEntity i "
                        + "where i.invoiceDate <= :asOf and i.invoiceStatus not in (:excluded) "
                        + "and (" + INV_TOTAL + " - coalesce(i.invoicePaidAmount, 0)) > 0 "
                        + "order by i.invoiceDate asc, i.invoicePrimeId asc", Object[].class)
                .setParameter("asOf", asOf).setParameter("excluded", excluded)
                .getResultList();
    }

    /** Bills dated up to asOf that still have a balance. Same columns as billRows. */
    public List<Object[]> openBills(LocalDate asOf) {
        return em.createQuery(BILL_SELECT + "where b.billDate <= :asOf" + BILL_GROUP
                        + "having b.amount - coalesce(sum(p.amount), 0) > 0.005 "
                        + "order by b.billDate asc, b.pbPrimeId asc", Object[].class)
                .setParameter("asOf", asOf)
                .getResultList();
    }

    /** Customer receipts not linked to any invoice. */
    public Object unappliedReceipts(LocalDate asOf) {
        return em.createQuery("select coalesce(sum(p.amount), 0) from CustomerPaymentEntity p "
                        + "where p.invoicePrimeId is null and (p.status is null or upper(p.status) = 'RECEIVED') "
                        + "and p.paymentDate <= :asOf")
                .setParameter("asOf", asOf).getSingleResult();
    }

    /** Total of applied-status vendor payments (paid / cleared). */
    public Object vendorPaymentsApplied(LocalDate asOf) {
        return em.createQuery("select coalesce(sum(p.amount), 0) from VendorPaymentEntity p "
                        + "where lower(p.status) in ('paid', 'cleared') and p.date <= :asOf")
                .setParameter("asOf", asOf).getSingleResult();
    }

    /** Part of those payments that was allocated to bills. */
    public Object vendorPaymentsAllocated(LocalDate asOf) {
        return em.createQuery("select coalesce(sum(a.amount), 0) from VendorPaymentEntity p join p.allocations a "
                        + "where lower(p.status) in ('paid', 'cleared') and p.date <= :asOf")
                .setParameter("asOf", asOf).getSingleResult();
    }

    // ---------------------------------------------------------------- stock
    /** One row per inventory line (product row or variant row, per warehouse), like the Inventory page.
     *  [0]item [1]variant [2]sku [3]category [4]unit [5]warehouse [6]stock [7]sellingPrice */
    public List<Object[]> stockRows() {
        return em.createQuery("select coalesce(p.productName, vp.productName), v.variantName, "
                        + "coalesce(v.variantSku, p.productSku), "
                        + "coalesce(v.variantCategory, vp.productCategory, p.productCategory), "
                        + "coalesce(v.variantUnit, vp.productUnit, p.productUnit), "
                        + "w.warehouseName, i.productStock, coalesce(v.variantSellingPrice, p.productSellingPrice) "
                        + "from InventoryEntity i left join i.product p left join i.variant v "
                        + "left join v.product vp join i.warehouse w", Object[].class)
                .getResultList();
    }

    // ---------------------------------------------------------------- accounting
    /** Approved expenses incl. their tax %, by category. [0]key [1]label [2]gross */
    public List<Object[]> expensesByCategory(LocalDate from, LocalDate to) {
        return em.createQuery("select lower(trim(e.expenseCategory)), min(trim(e.expenseCategory)), "
                        + "sum(e.expenseAmount + round(e.expenseAmount * coalesce(e.expenseTax, 0) / 100, 2)) "
                        + "from ExpenseEntity e where lower(e.expenseStatus) = 'approved' "
                        + "and e.expenseDate between :from and :to "
                        + "group by lower(trim(e.expenseCategory))", Object[].class)
                .setParameter("from", from).setParameter("to", to)
                .getResultList();
    }

    public Object receiptsUpTo(LocalDate asOf) {
        return em.createQuery("select coalesce(sum(p.amount), 0) from CustomerPaymentEntity p "
                        + "where (p.status is null or upper(p.status) = 'RECEIVED') and p.paymentDate <= :asOf")
                .setParameter("asOf", asOf).getSingleResult();
    }

    public Object billPaymentsUpTo(LocalDate asOf) {
        return em.createQuery("select coalesce(sum(p.amount), 0) from PurchaseBillPaymentEntity p where p.date <= :asOf")
                .setParameter("asOf", asOf).getSingleResult();
    }

    public Object approvedExpensesUpTo(LocalDate asOf) {
        return em.createQuery("select coalesce(sum(e.expenseAmount + round(e.expenseAmount * coalesce(e.expenseTax, 0) / 100, 2)), 0) "
                        + "from ExpenseEntity e where lower(e.expenseStatus) = 'approved' and e.expenseDate <= :asOf")
                .setParameter("asOf", asOf).getSingleResult();
    }


}