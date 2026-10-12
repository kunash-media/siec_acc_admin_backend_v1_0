package com.siec_acc.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "purchase_order_items")
public class PurchaseOrderItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long itemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "po_prime_id", nullable = false)
    private PurchaseOrderEntity purchaseOrder;

    @Column(name = "item_name", nullable = false)
    private String name;

    @Column(name = "qty", nullable = false)
    private Integer qty;

    @Column(name = "unit", length = 30)
    private String unit;

    @Column(name = "rate", nullable = false)
    private Double rate;

    // qty x rate, rounded to 2 dp. Calculated by the server (never accepted from the client).
    // Nullable only so existing rows load; PO_amount_migration.sql fills them.
    @Column(name = "amount")
    private Double amount;

    public PurchaseOrderItemEntity() {}

    public PurchaseOrderItemEntity(String name, Integer qty, String unit, Double rate) {
        this.name = name;
        this.qty = qty;
        this.unit = unit;
        this.rate = rate;
        recalculateAmount();
    }

    @PrePersist
    @PreUpdate
    public void recalculateAmount() {
        this.amount = (qty == null || rate == null) ? null : Math.round(qty * rate * 100.0) / 100.0;
    }

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }

    public PurchaseOrderEntity getPurchaseOrder() { return purchaseOrder; }
    public void setPurchaseOrder(PurchaseOrderEntity purchaseOrder) { this.purchaseOrder = purchaseOrder; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getQty() { return qty; }
    public void setQty(Integer qty) { this.qty = qty; recalculateAmount(); }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public Double getRate() { return rate; }
    public void setRate(Double rate) { this.rate = rate; recalculateAmount(); }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
}