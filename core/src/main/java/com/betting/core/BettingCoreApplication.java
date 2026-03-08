package com.betting.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy // Required for TenantFilterActivationAspect
public class BettingCoreApplication {
    public static void main(String[] args) {
        SpringApplication.run(BettingCoreApplication.class, args);
    }
}
