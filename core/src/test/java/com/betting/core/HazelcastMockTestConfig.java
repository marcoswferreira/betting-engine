package com.betting.core;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;
import static org.mockito.Mockito.when;

@Configuration
@Profile("test")
public class HazelcastMockTestConfig {

    @Bean
    @Primary
    public HazelcastInstance hazelcastInstance() {
        HazelcastInstance mockInstance = mock(HazelcastInstance.class);

        // Return a mock map by default so that @Autowired services can initialize their
        // fields
        IMap mockMap = mock(IMap.class, withSettings().extraInterfaces(java.util.concurrent.ConcurrentMap.class));
        when(mockInstance.getMap(anyString())).thenReturn(mockMap);

        return mockInstance;
    }
}
