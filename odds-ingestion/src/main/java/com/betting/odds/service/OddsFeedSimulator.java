package com.betting.odds.service;

import com.betting.odds.domain.MatchOdds;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Simulates an ETL process reading from an external sports XML/JSON feed.
 * It periodically updates live odds, saves them to Hazelcast (for low-latency REST API validation),
 * and publishes them to Kafka (for downstream async processing).
 */
@Service
public class OddsFeedSimulator {

    private static final Logger log = LoggerFactory.getLogger(OddsFeedSimulator.class);
    private static final String ODDS_TOPIC = "live-odds";
    private static final String HAZELCAST_MAP_NAME = "match-odds-map";

    private final KafkaTemplate<String, MatchOdds> kafkaTemplate;
    private final IMap<UUID, MatchOdds> oddsCache;
    private final Random random = new Random();

    // Mocking 2 live matches
    private final List<UUID> activeMatches = List.of(
            UUID.fromString("11111111-1111-1111-1111-111111111111"),
            UUID.fromString("22222222-2222-2222-2222-222222222222")
    );

    public OddsFeedSimulator(KafkaTemplate<String, MatchOdds> kafkaTemplate, HazelcastInstance hazelcastInstance) {
        this.kafkaTemplate = kafkaTemplate;
        this.oddsCache = hazelcastInstance.getMap(HAZELCAST_MAP_NAME);
    }

    @Scheduled(fixedRate = 5000)
    public void simulateLiveFeed() {
        for (UUID matchId : activeMatches) {
            MatchOdds odds = new MatchOdds(
                    matchId,
                    "Team A",
                    "Team B",
                    randomOdds(1.1, 3.0),
                    randomOdds(2.0, 4.0),
                    randomOdds(2.5, 8.0),
                    Instant.now()
            );

            // 1. Update High-Speed Cache (NoSQL alternative)
            oddsCache.put(matchId, odds);

            // 2. Publish to message broker (AMQP/Kafka)
            kafkaTemplate.send(ODDS_TOPIC, matchId.toString(), odds);

            log.info("Published new odds for match {}: Home {}, Draw {}, Away {}",
                    matchId, odds.homeWinOdds(), odds.drawOdds(), odds.awayWinOdds());
        }
    }

    private BigDecimal randomOdds(double min, double max) {
        double val = min + (max - min) * random.nextDouble();
        return BigDecimal.valueOf(val).setScale(2, RoundingMode.HALF_UP);
    }
}
