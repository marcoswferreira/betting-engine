package com.betting.odds;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OddsIngestionApplication {
    public static void main(String[] args) {
        SpringApplication.run(OddsIngestionApplication.class, args);
    }
}
