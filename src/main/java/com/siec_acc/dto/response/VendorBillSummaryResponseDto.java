package com.siec_acc.dto.response;
import java.time.LocalDate;

public class VendorBillSummaryResponseDto {

    private String vendorName;
    private int billCount;
    private double totalBilled;      // sum of bill totals (incl. GST)
    private double totalPaid;        // sum of payments recorded against the bills
    private double totalDue;         // totalBilled - totalPaid (what is still to be paid)
    private double overdueDue;       // part of totalDue whose bills are past their due date
    private int overdueBillCount;
    private LocalDate lastBillDate;
    private LocalDate lastPaymentDate;

    public VendorBillSummaryResponseDto() {}

    public VendorBillSummaryResponseDto(String vendorName) { this.vendorName = vendorName; }

    public String getVendorName() { return vendorName; }
    public void setVendorName(String vendorName) { this.vendorName = vendorName; }

    public int getBillCount() { return billCount; }
    public void setBillCount(int billCount) { this.billCount = billCount; }

    public double getTotalBilled() { return totalBilled; }
    public void setTotalBilled(double totalBilled) { this.totalBilled = totalBilled; }

    public double getTotalPaid() { return totalPaid; }
    public void setTotalPaid(double totalPaid) { this.totalPaid = totalPaid; }

    public double getTotalDue() { return totalDue; }
    public void setTotalDue(double totalDue) { this.totalDue = totalDue; }

    public double getOverdueDue() { return overdueDue; }
    public void setOverdueDue(double overdueDue) { this.overdueDue = overdueDue; }

    public int getOverdueBillCount() { return overdueBillCount; }
    public void setOverdueBillCount(int overdueBillCount) { this.overdueBillCount = overdueBillCount; }

    public LocalDate getLastBillDate() { return lastBillDate; }
    public void setLastBillDate(LocalDate lastBillDate) { this.lastBillDate = lastBillDate; }

    public LocalDate getLastPaymentDate() { return lastPaymentDate; }
    public void setLastPaymentDate(LocalDate lastPaymentDate) { this.lastPaymentDate = lastPaymentDate; }
}
