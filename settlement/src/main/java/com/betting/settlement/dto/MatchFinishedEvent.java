package com.betting.settlement.dto;

import java.util.UUID;

public record MatchFinishedEvent(
        UUID matchId,
        String winningPrediction // HOME_WIN, DRAW, AWAY_WIN
) {}
