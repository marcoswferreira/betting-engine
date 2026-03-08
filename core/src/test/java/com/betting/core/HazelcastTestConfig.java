package com.betting.core;

import com.betting.core.domain.MatchOdds;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestConfiguration
public class HazelcastTestConfig {

    @Bean
    @Primary
    public HazelcastInstance hazelcastTestInstance() {
        HazelcastInstance mockInstance = mock(HazelcastInstance.class);
        IMap<UUID, MatchOdds> mockMap = mock(IMap.class);
        when(mockInstance.<UUID, MatchOdds>getMap("match-odds-map")).thenReturn(mockMap);
        return mockInstance;
    }
}
