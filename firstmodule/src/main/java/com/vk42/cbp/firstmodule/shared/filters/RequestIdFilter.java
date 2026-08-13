package com.vk42.cbp.firstmodule.shared.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class RequestIdFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // search if client already sent an ID. If not, we create one
        String requestId = request.getHeader("X-Request-ID");
        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString();
        }
        // Mapped Diagnostic Context - logs
        MDC.put("request_id", requestId);
        // return to client
        response.setHeader("X-Request-ID", requestId);

        try {
            // let request go to controller
            filterChain.doFilter(request, response);
        } finally {
            // clean memory
            MDC.remove("request_id");
        }
    }
}
