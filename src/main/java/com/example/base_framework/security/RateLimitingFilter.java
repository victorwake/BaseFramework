package com.example.base_framework.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Order(1)
public class RateLimitingFilter extends OncePerRequestFilter {

    private final ConcurrentHashMap<String, RateLimitEntry> attempts = new ConcurrentHashMap<>();
    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_MS = 60_000;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.equals("/auth/login");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String ip = getClientIP(request);
        RateLimitEntry entry = attempts.computeIfAbsent(ip, k -> new RateLimitEntry());

        synchronized (entry) {
            if (entry.isBlocked()) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Demasiados intentos, espere 1 minuto\"}");
                return;
            }
            entry.increment();
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIP(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            return xf.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static class RateLimitEntry {
        private final AtomicInteger count = new AtomicInteger(1);
        private final long windowStart = System.currentTimeMillis();

        boolean isBlocked() {
            if (System.currentTimeMillis() - windowStart > WINDOW_MS) {
                return false;
            }
            return count.get() > MAX_ATTEMPTS;
        }

        void increment() {
            if (System.currentTimeMillis() - windowStart <= WINDOW_MS) {
                count.incrementAndGet();
            }
        }
    }
}
