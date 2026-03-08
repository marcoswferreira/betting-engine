package com.betting.core.config;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class HazelcastConfig {

    @Value("${hazelcast.network.cluster-members:localhost:5701}")
    private String clusterMembers;

    @Bean
    public HazelcastInstance hazelcastInstance() {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.getNetworkConfig().addAddress(clusterMembers.split(","));
        clientConfig.setInstanceName("core-api-client");
        return HazelcastClient.newHazelcastClient(clientConfig);
    }
}
