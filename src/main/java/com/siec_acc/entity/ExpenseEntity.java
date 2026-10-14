package com.siec_acc.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "expenses", indexes = {
        @Index(name = "idx_expense_date", columnList = "expense_date"),
        @Index(name = "idx_expense_status", columnList = "expense_status"),
        @Index(name = "idx_expense_category", columnList = "expense_category")
})
public class ExpenseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expense_prime_id")
    private Long expensePrimeId;

    @Column(name = "expense_str_id", unique = true, length = 30)
    private String expenseStrId;

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Column(name = "expense_category", nullable = false, length = 100)
    private String expenseCategory;

    @Column(name = "expense_description", nullable = false, length = 500)
    private String expenseDescription;

    @Column(name = "expense_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal expenseAmount;

    @Column(name = "expense_tax", precision = 5, scale = 2)
    private BigDecimal expenseTax;

    @Column(name = "expense_vendor", length = 255)
    private String expenseVendor;

    @Column(name = "expense_payment_account", length = 50)
    private String expensePaymentAccount;

    @Column(name = "expense_status", nullable = false, length = 20)
    private String expenseStatus;

    @Column(name = "expense_project_id", length = 30)
    private String expenseProjectId;

    @Column(name = "expense_notes", columnDefinition = "TEXT")
    private String expenseNotes;

    @Column(name = "expense_receipt", length = 500)
    private String expenseReceipt;

    @Column(name = "expense_created_at")
    private LocalDateTime expenseCreatedAt;

    @Column(name = "expense_updated_at")
    private LocalDateTime expenseUpdatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.expenseCreatedAt = now;
        this.expenseUpdatedAt = now;
        if (this.expenseStatus == null) this.expenseStatus = "pending";
    }

    @PreUpdate
    protected void onUpdate() {
        this.expenseUpdatedAt = LocalDateTime.now();
    }

    public Long getExpensePrimeId() { return expensePrimeId; }
    public void setExpensePrimeId(Long expensePrimeId) { this.expensePrimeId = expensePrimeId; }

    public String getExpenseStrId() { return expenseStrId; }
    public void setExpenseStrId(String expenseStrId) { this.expenseStrId = expenseStrId; }

    public LocalDate getExpenseDate() { return expenseDate; }
    public void setExpenseDate(LocalDate expenseDate) { this.expenseDate = expenseDate; }

    public String getExpenseCategory() { return expenseCategory; }
    public void setExpenseCategory(String expenseCategory) { this.expenseCategory = expenseCategory; }

    public String getExpenseDescription() { return expenseDescription; }
    public void setExpenseDescription(String expenseDescription) { this.expenseDescription = expenseDescription; }

    public BigDecimal getExpenseAmount() { return expenseAmount; }
    public void setExpenseAmount(BigDecimal expenseAmount) { this.expenseAmount = expenseAmount; }

    public BigDecimal getExpenseTax() { return expenseTax; }
    public void setExpenseTax(BigDecimal expenseTax) { this.expenseTax = expenseTax; }

    public String getExpenseVendor() { return expenseVendor; }
    public void setExpenseVendor(String expenseVendor) { this.expenseVendor = expenseVendor; }

    public String getExpensePaymentAccount() { return expensePaymentAccount; }
    public void setExpensePaymentAccount(String expensePaymentAccount) { this.expensePaymentAccount = expensePaymentAccount; }

    public String getExpenseStatus() { return expenseStatus; }
    public void setExpenseStatus(String expenseStatus) { this.expenseStatus = expenseStatus; }

    public String getExpenseProjectId() { return expenseProjectId; }
    public void setExpenseProjectId(String expenseProjectId) { this.expenseProjectId = expenseProjectId; }

    public String getExpenseNotes() { return expenseNotes; }
    public void setExpenseNotes(String expenseNotes) { this.expenseNotes = expenseNotes; }

    public String getExpenseReceipt() { return expenseReceipt; }
    public void setExpenseReceipt(String expenseReceipt) { this.expenseReceipt = expenseReceipt; }

    public LocalDateTime getExpenseCreatedAt() { return expenseCreatedAt; }
    public void setExpenseCreatedAt(LocalDateTime expenseCreatedAt) { this.expenseCreatedAt = expenseCreatedAt; }

    public LocalDateTime getExpenseUpdatedAt() { return expenseUpdatedAt; }
    public void setExpenseUpdatedAt(LocalDateTime expenseUpdatedAt) { this.expenseUpdatedAt = expenseUpdatedAt; }
}