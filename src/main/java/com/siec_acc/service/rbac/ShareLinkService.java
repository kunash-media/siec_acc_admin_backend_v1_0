package com.siec_acc.service.rbac;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;

@Service
public class ShareLinkService {

    public enum DocType { QUOTE, INVOICE, PROFORMA, DC }

    private final byte[] key;
    private final long ttlSeconds;
    private final String baseUrl;

    public ShareLinkService(@Value("${app.share.secret}") String secret,
                            @Value("${app.share.ttl-days:30}") long ttlDays,
                            @Value("${app.public.base-url}") String baseUrl) {
        if (secret == null || secret.length() < 32)
            throw new IllegalStateException("app.share.secret must be at least 32 characters");
        this.key = secret.getBytes(StandardCharsets.UTF_8);
        this.ttlSeconds = ttlDays * 86400L;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    public long expiryFromNow() {
        return Instant.now().getEpochSecond() + ttlSeconds;
    }

    /** Absolute, signed, expiring public URL for one document. */
    public String buildUrl(DocType type, String id) {
        long exp = expiryFromNow();
        return baseUrl + "/api/public/share/" + type.name() + "/" + id
                + "?exp=" + exp + "&sig=" + sign(type, id, exp);
    }

    public boolean verify(DocType type, String id, long exp, String sig) {
        if (sig == null || exp < Instant.now().getEpochSecond()) return false;
        byte[] expected = sign(type, id, exp).getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expected, sig.getBytes(StandardCharsets.UTF_8));
    }

    private String sign(DocType type, String id, long exp) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            byte[] raw = mac.doFinal((type.name() + "|" + id + "|" + exp).getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        } catch (Exception e) {
            throw new IllegalStateException("Could not sign share link", e);
        }
    }
}