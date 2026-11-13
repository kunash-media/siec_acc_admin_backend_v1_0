package com.siec_acc.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Indian-rupee formatting (lakh / crore grouping). Java's DecimalFormat cannot do 2-digit grouping. */
public final class IndianCurrency {

    private static final String RUPEE = "\u20B9";
    private static final BigDecimal LAKH = new BigDecimal("100000");
    private static final BigDecimal CRORE = new BigDecimal("10000000");
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private IndianCurrency() {}

    /** 14186500 -> ₹1,41,86,500 ; paise are shown only when non-zero. */
    public static String full(BigDecimal value) {
        BigDecimal v = scale(value);
        return (v.signum() < 0 ? "-" : "") + RUPEE + group(v.abs());
    }

    /** Same as full() but with an explicit '+' for positive values (used for Net). */
    public static String signedFull(BigDecimal value) {
        BigDecimal v = scale(value);
        String sign = v.signum() > 0 ? "+" : v.signum() < 0 ? "-" : "";
        return sign + RUPEE + group(v.abs());
    }

    /** 4250000 -> ₹42.5L ; 12500000 -> ₹1.25Cr ; below one lakh the full grouped amount is shown. */
    public static String compact(BigDecimal value) {
        BigDecimal v = scale(value);
        String sign = v.signum() < 0 ? "-" : "";
        BigDecimal abs = v.abs();
        if (abs.compareTo(CRORE) >= 0) {
            return sign + RUPEE + plain(abs.divide(CRORE, 2, RoundingMode.HALF_UP)) + "Cr";
        }
        if (abs.compareTo(LAKH) >= 0) {
            BigDecimal lakhs = abs.divide(LAKH, 2, RoundingMode.HALF_UP);
            if (lakhs.compareTo(HUNDRED) >= 0) return sign + RUPEE + "1Cr"; // 99,99,999.x rounds up to 100L
            return sign + RUPEE + plain(lakhs) + "L";
        }
        return sign + RUPEE + group(abs);
    }

    private static BigDecimal scale(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }

    private static String plain(BigDecimal d) {
        return d.stripTrailingZeros().toPlainString();
    }

    private static String group(BigDecimal abs) {
        String[] parts = abs.toPlainString().split("\\.");
        String intPart = parts[0];
        String frac = parts.length > 1 ? parts[1] : "00";
        String grouped;
        if (intPart.length() <= 3) {
            grouped = intPart;
        } else {
            String last3 = intPart.substring(intPart.length() - 3);
            String rest = intPart.substring(0, intPart.length() - 3);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < rest.length(); i++) {
                sb.append(rest.charAt(i));
                int remaining = rest.length() - 1 - i;
                if (remaining > 0 && remaining % 2 == 0) sb.append(',');
            }
            grouped = sb + "," + last3;
        }
        return "00".equals(frac) ? grouped : grouped + "." + frac;
    }
}