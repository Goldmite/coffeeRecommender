package org.recsys.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "recommendation.hybrid")
@Getter
@Setter
public class HybridConfig {
    // cbf is implied, both weights combined = 1
    private float cf = 0.5f;
    // increase cf weight per user interaction
    private float cfAdaptationRate = 0.1f;
}
