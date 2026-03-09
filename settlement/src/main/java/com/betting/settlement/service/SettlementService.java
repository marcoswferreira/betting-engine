package com.betting.settlement.service;

import com.betting.common.dto.MatchFinishedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SettlementService {

    private static final Logger log = LoggerFactory.getLogger(SettlementService.class);
    private final JdbcTemplate jdbcTemplate;

    public SettlementService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @KafkaListener(topics = "match-finished", groupId = "settlement-group-v2")
    @Transactional
    public void handleMatchFinished(MatchFinishedEvent event) {
        log.info("Received Match Finished Event for Match: {} with Outcome: {}", 
                event.matchId(), event.winningPrediction());

        // 1. Mark winning bets as WON and losing bets as LOST in bulk
        int wonCount = jdbcTemplate.update("""
                UPDATE bet
                SET status = 'WON'
                WHERE match_id = ? AND prediction = ? AND status = 'PENDING'
                """, event.matchId(), event.winningPrediction());

        int lostCount = jdbcTemplate.update("""
                UPDATE bet
                SET status = 'LOST'
                WHERE match_id = ? AND prediction != ? AND status = 'PENDING'
                """, event.matchId(), event.winningPrediction());
                
        // 2. Process payouts directly updating wallets.
        // Formula: stake * placed_odds
        // We use an atomic UPDATE statement to bypass reading thousands of records into Hibernate JVM memory
        // Multi-Tenancy applies here automatically because the JOIN matches specific bet users to their exact tenant isolated wallet
        int payoutCount = jdbcTemplate.update("""
                UPDATE wallet w
                SET balance = w.balance + (b.stake * b.placed_odds)
                FROM bet b
                WHERE b.user_id = w.user_id
                  AND b.match_id = ?
                  AND b.prediction = ?
                  AND b.status = 'WON'
                """, event.matchId(), event.winningPrediction());

        log.info("Settlement Complete! {} won bets, {} lost bets, {} wallets credited.", wonCount, lostCount, payoutCount);

        // NOTE: In a production ledger, we would also insert records into the `transaction` table here.
    }
}
