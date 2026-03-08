package com.betting.core;

import com.betting.core.domain.AppUser;
import com.betting.core.domain.MatchOdds;
import com.betting.core.domain.Wallet;
import com.betting.core.dto.BetRequest;
import com.betting.core.infra.tenant.TenantContext;
import com.betting.core.repository.AppUserRepository;
import com.betting.core.repository.BetRepository;
import com.betting.core.repository.WalletRepository;
import com.betting.core.service.BettingService;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.PostgreSQLContainer;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.mockito.Mockito;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("test")
@SpringBootTest(properties = {
    "spring.hazelcast.config=",
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.hazelcast.HazelcastAutoConfiguration",
    "spring.main.allow-bean-definition-overriding=true"
})
@Testcontainers
@Import(HazelcastMockTestConfig.class)
class BettingServiceConcurrencyTest {
    
    static {
        System.setProperty("net.bytebuddy.experimental", "true");
    }

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("betting_test")
            .withUsername("test")
            .withPassword("test");

    @Autowired private BettingService bettingService;
    @Autowired private WalletRepository walletRepository;
    @Autowired private AppUserRepository appUserRepository;
    @Autowired private BetRepository betRepository;
    @Autowired private HazelcastInstance hazelcastInstance;

    private UUID tenantId;
    private AppUser user;
    private UUID matchId;

    @BeforeEach
    void setup() {
        betRepository.deleteAll();
        walletRepository.deleteAll();
        appUserRepository.deleteAll();

        tenantId = UUID.randomUUID();
        user = appUserRepository.save(new AppUser(UUID.randomUUID(), tenantId, "testuser"));
        
        // Give the user exactly $100.00
        Wallet wallet = new Wallet(tenantId, user, new BigDecimal("100.00"));
        walletRepository.save(wallet);

        matchId = UUID.randomUUID();
        
        // The BettingService already obtained a mock map during construction.
        // We obtain the SAME mock map from the hazelcastInstance mock and stub it.
        IMap<UUID, MatchOdds> mockCache = hazelcastInstance.getMap("match-odds-map");
        
        Mockito.when(mockCache.get(matchId)).thenReturn(new MatchOdds(
                matchId, "Team A", "Team B",
                new BigDecimal("2.50"), new BigDecimal("3.00"), new BigDecimal("2.80"),
                Instant.now()
        ));
    }

    @AfterEach
    void cleanup() {
        TenantContext.clear();
    }

    @Test
    void testConcurrentBetting_ShouldNotDropBalanceBelowZero() throws InterruptedException {
        // Assume context is set by the web filter in real life
        TenantContext.setTenantId(tenantId);

        int numberOfThreads = 100;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failedCount = new AtomicInteger();

        // 100 threads will try to place a $5.00 bet simultaneously.
        // The wallet only has $100.00. 
        // Exactly 20 bets should succeed. 80 should fail due to Insufficient Funds or OptimisticLock.
        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                try {
                    // It is critical that TenantContext is passed down to the thread execution
                    TenantContext.setTenantId(tenantId);
                    
                    BetRequest request = new BetRequest(matchId, "HOME_WIN", new BigDecimal("2.50"), new BigDecimal("5.00"));
                    bettingService.placeBet(user.getId(), request);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failedCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // Verification
        Wallet finalWallet = walletRepository.findByUserId(user.getId()).orElseThrow();
        
        System.out.println("Successful Bets: " + successCount.get());
        System.out.println("Failed Bets: " + failedCount.get());
        System.out.println("Final Wallet Balance: " + finalWallet.getBalance());

        // Assert that the balance never dropped below zero and exactly 20 bets went through
        assertEquals(0, new BigDecimal("0.00").compareTo(finalWallet.getBalance()), "Balance should be exactly $0.00");
        assertEquals(20, betRepository.count(), "Exactly 20 bet records should exist");
        assertEquals(20, successCount.get(), "Exactly 20 threads should have succeeded");
    }
}
