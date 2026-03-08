package com.betting.core.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record BetRequest(
        @NotNull UUID matchId,
        @NotNull String prediction, // HOME_WIN, DRAW, AWAY_WIN
        @NotNull @DecimalMin("1.01") BigDecimal requestedOdds,
        @NotNull @DecimalMin("0.50") BigDecimal stake
) {}
