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
            Map<String, Object> m = objectMapper.convertValue(body, new TypeReference<Map<String, Object>>() {});
            http.setAttribute(AdminAccessInterceptor.ATTR_REF, extract(m));
        } catch (Exception e) {
            logger.debug("[rbac] entity ref not extractable: {}", e.getMessage());
        }
        return body;
    }

    // <x>PrimeId + <x>StrId pair with the shortest prefix (avoids quoteClientPrimeId etc.); fallback id/number (Sales Orders)
    private static String[] extract(Map<String, Object> m) {
        String best = null;
        for (String k : m.keySet()) {
            if (k.endsWith("StrId") && k.length() > 5) {
                String p = k.substring(0, k.length() - 5);
                if (m.containsKey(p + "PrimeId") && (best == null || p.length() < best.length())) best = p;
            }
        }
        if (best != null) return new String[]{ str(m.get(best + "PrimeId")), str(m.get(best + "StrId")) };
        return new String[]{ str(m.get("id")), str(m.get("number")) };
    }

    private static String str(Object o) { return o == null ? null : String.valueOf(o); }
}