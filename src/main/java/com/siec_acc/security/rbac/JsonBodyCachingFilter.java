//package com.siec_acc.security.rbac;
//
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//
//@Component
//public class JsonBodyCachingFilter extends OncePerRequestFilter {
//
//    @Override
//    protected boolean shouldNotFilter(HttpServletRequest r) {
//        String m = r.getMethod(), ct = r.getContentType();
//        return !("POST".equals(m) || "PUT".equals(m) || "PATCH".equals(m))
//                || ct == null || !ct.toLowerCase().startsWith("application/json")
//                || !r.getRequestURI().startsWith("/api/")
//                || r.getContentLengthLong() > 262144;     // never buffer large bodies
//    }
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
//            throws ServletException, IOException {
//        chain.doFilter(new CachedBodyRequest(req), res);
//    }
//}