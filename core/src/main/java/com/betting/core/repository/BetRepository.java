package com.betting.core.repository;

import com.betting.core.domain.Bet;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface BetRepository extends JpaRepository<Bet, UUID> {}
