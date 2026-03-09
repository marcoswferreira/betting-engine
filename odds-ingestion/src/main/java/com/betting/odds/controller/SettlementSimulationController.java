package com.betting.odds.controller;

import com.betting.common.dto.MatchFinishedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/simulation")
public class SettlementSimulationController {

    private static final Logger log = LoggerFactory.getLogger(SettlementSimulationController.class);
    private static final String MATCH_FINISHED_TOPIC = "match-finished";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public SettlementSimulationController(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping("/match-finish")
    public String simulateMatchFinish(@RequestBody MatchFinishedEvent event) {
        log.info("Simulating match finish for match: {} with outcome: {}", 
                event.matchId(), event.winningPrediction());
        
        kafkaTemplate.send(MATCH_FINISHED_TOPIC, event.matchId().toString(), event);
        
        return "Match finish event published for match " + event.matchId();
    }
}
