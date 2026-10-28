package com.siec_acc.security.rbac;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@RestControllerAdvice
public class EntityRefCaptureAdvice implements ResponseBodyAdvice<Object> {

    private static final Logger logger = LoggerFactory.getLogger(EntityRefCaptureAdvice.class);
    private final ObjectMapper objectMapper;

    public EntityRefCaptureAdvice(ObjectMapper objectMapper) { this.objectMapper = objectMapper; }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) { return true; }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType contentType,
                                  Class<? extends HttpMessageConverter<?>> converterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        if (body == null || body instanceof byte[] || body instanceof CharSequence) return body;
        if (!(request instanceof ServletServerHttpRequest sr)) return body;

        HttpServletRequest http = sr.getServletRequest();
        if (!AdminAccessInterceptor.isMutating(http.getMethod())) return body;

        try {
            Map<String, Object> m = toMap(body);
            if (m.get("data") instanceof Map<?, ?> inner) m = toMap(inner);   // ApiResponse / {success,message,data} envelopes
            http.setAttribute(AdminAccessInterceptor.ATTR_REF, extract(m));
        } catch (Exception e) {
            logger.debug("[rbac] entity ref not extractable: {}", e.getMessage());
        }
        return body;
    }

    private Map<String, Object> toMap(Object o) {
        return objectMapper.convertValue(o, new TypeReference<Map<String, Object>>() {});
    }

    // returns { id, number, amount, status }
    private static String[] extract(Map<String, Object> m) {
        String id, ref;
        if (m.get("paymentId") != null) {                       // customer payment (has clientPrimeId/clientStrId too, so check first)
            id = str(m.get("paymentId")); ref = str(m.get("receiptNumber"));
        } else if (m.get("vpStrId") != null) {                  // vendor payment
            id = str(m.get("vpStrId")); ref = str(m.get("vpNumber"));
        } else {
            String best = null;                                 // <x>PrimeId + <x>StrId pair, shortest prefix wins
            for (String k : m.keySet()) {
                if (k.endsWith("StrId") && k.length() > 5) {
                    String p = k.substring(0, k.length() - 5);
                    if (m.containsKey(p + "PrimeId") && (best == null || p.length() < best.length())) best = p;
                }
            }
            if (best != null) { id = str(m.get(best + "PrimeId")); ref = str(m.get(best + "StrId")); }
            else { id = str(m.get("id")); ref = str(m.get("number")); }      // sales orders
        }
        return new String[]{ id, ref, amount(m), str(m.get("status")) };
    }

    private static String amount(Map<String, Object> m) {
        Object a = m.get("amount");
        if (a == null) a = m.get("total");
        if (a == null) for (String k : m.keySet()) if (k.endsWith("TotalAmountInr")) { a = m.get(k); break; }
        if (a == null) for (String k : m.keySet()) if (k.endsWith("TotalAmount")) { a = m.get(k); break; }
        return str(a);
    }

    private static String str(Object o) { return o == null ? null : String.valueOf(o); }
}