package com.example.base_framework.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

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
                long retryAfterSeconds = Math.max(0, (entry.windowStart + WINDOW_MS - System.currentTimeMillis()) / 1000);

                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
                response.getWriter().write(
                        "{\"error\":\"Demasiados intentos. Has superado el límite de " + MAX_ATTEMPTS +
                                " intentos permitidos. Espera " + retryAfterSeconds +
                                " segundos antes de volver a intentar.\"}"
                );
                response.getWriter().flush();
                return;
            }
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
        private volatile long windowStart = System.currentTimeMillis();

        boolean isBlocked() {
            long now = System.currentTimeMillis();
            if (now - windowStart > WINDOW_MS) {
                windowStart = now;
                count.set(1);
                return false;
            }
            int current = count.getAndIncrement();
            return current > MAX_ATTEMPTS;
        }
    }
}
