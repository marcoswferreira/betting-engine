package com.betting.core.infra.init;

import com.betting.core.domain.AppUser;
import com.betting.core.domain.Wallet;
import com.betting.core.repository.AppUserRepository;
import com.betting.core.repository.WalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final AppUserRepository userRepository;
    private final WalletRepository walletRepository;

    public DataInitializer(AppUserRepository userRepository, WalletRepository walletRepository) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        UUID testUserId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID tenantId = UUID.fromString("11111111-1111-1111-1111-111111111111");

        if (userRepository.existsById(testUserId)) {
            log.info("Test user already exists. Skipping seeding.");
            return;
        }

        log.info("Seeding initial data (Test User and Wallet)...");

        AppUser user = new AppUser(testUserId, tenantId, "test_user");
        user = userRepository.saveAndFlush(user);

        Wallet wallet = new Wallet(tenantId, user, new BigDecimal("1000.00"));
        walletRepository.saveAndFlush(wallet);

        log.info("Initial data seeded successfully. Test User ID: {}", testUserId);
    }
}
