package com.betting.core.service;

import com.betting.core.domain.Bet;
import com.betting.core.domain.MatchOdds;
import com.betting.core.domain.Wallet;
import com.betting.core.dto.BetRequest;
import com.betting.core.infra.tenant.TenantContext;
import com.betting.core.repository.BetRepository;
import com.betting.core.repository.WalletRepository;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class BettingService {

    private static final Logger log = LoggerFactory.getLogger(BettingService.class);
    private static final String HAZELCAST_MAP_NAME = "match-odds-map";

    private final WalletRepository walletRepository;
    private final BetRepository betRepository;
    private final IMap<UUID, MatchOdds> oddsCache;

    // JVM-level concurrency: Prevent the SAME user from placing two simultaneous requests 
    // that might bypass initial validation before DB locks kick in.
    private final ConcurrentHashMap<UUID, Lock> userLocks = new ConcurrentHashMap<>();

    public BettingService(WalletRepository walletRepository,
                          BetRepository betRepository,
                          HazelcastInstance hazelcastInstance) {
        this.walletRepository = walletRepository;
        this.betRepository = betRepository;
        this.oddsCache = hazelcastInstance.getMap(HAZELCAST_MAP_NAME);
    }

    /**
     * Core routine for placing a bet. Ensures:
     * 1. Live odds haven't changed since the user clicked 'Bet'.
     * 2. Wallet has sufficient funds.
     * 3. Concurrent requests for the same wallet are serialized to avoid race conditions.
     */
    @Transactional
    public Bet placeBet(UUID userId, BetRequest request) {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) throw new IllegalStateException("No tenant in context");

        Lock lock = userLocks.computeIfAbsent(userId, k -> new ReentrantLock());
        lock.lock();
        
        try {
            // 1. Validate Live Odds (Distributed NoSQL read - extremely fast)
            MatchOdds liveOdds = oddsCache.get(request.matchId());
            if (liveOdds == null) {
                throw new IllegalArgumentException("Match not found or not live");
            }

            BigDecimal currentOdd = switch (request.prediction()) {
                case "HOME_WIN" -> liveOdds.homeWinOdds();
                case "DRAW" -> liveOdds.drawOdds();
                case "AWAY_WIN" -> liveOdds.awayWinOdds();
                default -> throw new IllegalArgumentException("Invalid prediction");
            };

            if (currentOdd.compareTo(request.requestedOdds()) != 0) {
                throw new IllegalStateException("Odds have changed! Current: " + currentOdd + ", Requested: " + request.requestedOdds());
            }

            // 2. Wallet deduction (Relies on @Version Optimistic Locking + JVM Lock)
            Wallet wallet = walletRepository.findByUserId(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Wallet not found for user"));

            wallet.deduct(request.stake());
            walletRepository.save(wallet);

            // 3. Create Bet Record
            Bet bet = new Bet(
                    tenantId,
                    wallet.getUser(),
                    request.matchId(),
                    request.prediction(),
                    currentOdd,
                    request.stake()
            );
            
            bet = betRepository.save(bet);
            
            log.info("Bet {} placed successfully by user {} for match {} at odds {}", 
                    bet.getId(), userId, bet.getMatchId(), currentOdd);
                    
            return bet;
            
        } finally {
            lock.unlock();
            // Cleanup lock map periodically or leave if bounded users
        }
    }
}
