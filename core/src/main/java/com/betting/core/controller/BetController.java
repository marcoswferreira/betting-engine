package com.betting.core.controller;

import com.betting.core.domain.Bet;
import com.betting.core.dto.BetRequest;
import com.betting.core.service.BettingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/bets")
public class BetController {

    private final BettingService bettingService;

    public BetController(BettingService bettingService) {
        this.bettingService = bettingService;
    }

    @PostMapping("/{userId}")
    public ResponseEntity<Bet> placeBet(
            @PathVariable UUID userId,
            @Valid @RequestBody BetRequest request) {
            
        Bet bet = bettingService.placeBet(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(bet);
    }
}
