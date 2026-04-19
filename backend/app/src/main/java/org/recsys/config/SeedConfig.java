package org.recsys.config;

import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SeedConfig {

    @Bean
    public Random seededRandom(@Value("${app.testdata.seed:1}") long seed) {
        return new Random(seed);
    }
}