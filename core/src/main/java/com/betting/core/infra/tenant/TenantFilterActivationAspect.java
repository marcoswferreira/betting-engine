package com.betting.core.infra.tenant;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Activates the Hibernate {@code tenantFilter} on the current session before
 * any
 * service-layer method executes. This ensures that all Hibernate queries for
 * {@link com.betting.core.domain.Wallet} and {@link com.betting.core.domain.Bet} automatically include
 * {@code WHERE tenant_id = :tenantId}.
 */
@Aspect
@Component
public class TenantFilterActivationAspect {

    @PersistenceContext
    private EntityManager entityManager;

    @Before("execution(* com.betting.core.service..*(..))")
    public void enableTenantFilter() {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return;
        }
        Session session = entityManager.unwrap(Session.class);
        session.enableFilter("tenantFilter").setParameter("tenantId", tenantId);
    }
}
