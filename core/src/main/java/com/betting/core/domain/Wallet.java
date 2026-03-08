package com.betting.core.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.Filter;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "wallet")
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public class Wallet {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @Version
    @Column(nullable = false)
    private Long version;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();

    protected Wallet() {}

    public Wallet(UUID tenantId, AppUser user, BigDecimal initialBalance) {
        this.tenantId = tenantId;
        this.user = user;
        this.balance = initialBalance;
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public AppUser getUser() { return user; }
    public BigDecimal getBalance() { return balance; }
    public Long getVersion() { return version; }

    public void deduct(BigDecimal amount) {
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds");
        }
        this.balance = this.balance.subtract(amount);
    }
    
    public void credit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }
}
