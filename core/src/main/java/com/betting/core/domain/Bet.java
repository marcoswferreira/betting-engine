package com.betting.core.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.Filter;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "bet")
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public class Bet {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "match_id", nullable = false)
    private UUID matchId;

    @Column(nullable = false)
    private String prediction; // HOME_WIN, DRAW, AWAY_WIN

    @Column(name = "placed_odds", nullable = false)
    private BigDecimal placedOdds;

    @Column(nullable = false)
    private BigDecimal stake;

    @Column(nullable = false)
    private String status = "PENDING"; // WON, LOST, CANCELLED

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();

    protected Bet() {}

    public Bet(UUID tenantId, AppUser user, UUID matchId, String prediction, BigDecimal odds, BigDecimal stake) {
        this.tenantId = tenantId;
        this.user = user;
        this.matchId = matchId;
        this.prediction = prediction;
        this.placedOdds = odds;
        this.stake = stake;
    }
    
    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public UUID getMatchId() { return matchId; }
    public String getStatus() { return status; }
    public BigDecimal getStake() { return stake; }
    public BigDecimal getPlacedOdds() { return placedOdds; }
}
