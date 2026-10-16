package com.siec_acc.service;

import com.siec_acc.dto.request.VendorPaymentRequestDto;
import com.siec_acc.dto.response.VendorPaymentResponseDto;

import java.util.List;

public interface VendorPaymentService {

    VendorPaymentResponseDto createVendorPayment(VendorPaymentRequestDto dto);

    VendorPaymentResponseDto updateVendorPayment(String vpStrId, VendorPaymentRequestDto dto);

    void deleteVendorPayment(String vpStrId);

    VendorPaymentResponseDto getVendorPayment(String vpStrId);

    /** All payments, newest first. Every filter is optional (null / blank / "all" = no filter). */
    List<VendorPaymentResponseDto> getAllVendorPayments(String mode, String vendorId, String search);

    VendorPaymentResponseDto.VendorPaymentStatsResponseDto getStats();

    /** Bills of the vendor that still have a balance. With excludeVpStrId, what that payment already applied is added back. */
    List<VendorPaymentResponseDto.VendorOpenBillResponseDto> getOpenBillsOfVendor(String vendorId, String excludeVpStrId);
}
