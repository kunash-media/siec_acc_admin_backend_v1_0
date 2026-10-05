package com.siec_acc.utils;

public final class VendorIdGenerator {

    private static final String PREFIX = "VND-";

    private VendorIdGenerator() {
        // Utility class
    }

    public static String generate(Long vendorPrimeId) {
        if (vendorPrimeId == null || vendorPrimeId <= 0) {
            throw new IllegalArgumentException("vendorPrimeId must be generated before creating vendorStrId");
        }

        return PREFIX + String.format("%06d", vendorPrimeId);
    }
}

