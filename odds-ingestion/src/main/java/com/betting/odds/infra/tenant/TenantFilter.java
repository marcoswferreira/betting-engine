package com.betting.odds.infra.tenant;

import com.betting.odds.infra.security.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class TenantFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(TenantFilter.class);
    private static final String HEADER_TENANT_ID = "X-Tenant-ID";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    public TenantFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        try {
            String tenantId = resolveTenantId(request);
            if (StringUtils.hasText(tenantId)) {
                TenantContext.setTenantId(UUID.fromString(tenantId));
                log.debug("Tenant resolved: {}", tenantId);
            }
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private String resolveTenantId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith(BEARER_PREFIX)) {
            String token = authHeader.substring(BEARER_PREFIX.length());
            try {
                String tenantId = jwtUtil.extractTenantId(token);
                if (StringUtils.hasText(tenantId)) {
                    return tenantId;
                }
            } catch (Exception e) {
                log.debug("Could not extract tenant_id from JWT: {}", e.getMessage());
            }
        }

        String headerTenant = request.getHeader(HEADER_TENANT_ID);
        if (StringUtils.hasText(headerTenant)) {
            return headerTenant;
        }

        return null;
    }
}
