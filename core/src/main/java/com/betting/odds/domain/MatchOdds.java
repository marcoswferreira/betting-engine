package com.betting.odds.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record MatchOdds(
        UUID matchId,
        String homeTeam,
        String awayTeam,
        BigDecimal homeWinOdds,
        BigDecimal drawOdds,
        BigDecimal awayWinOdds,
        Instant updatedAt
) implements Serializable {}
