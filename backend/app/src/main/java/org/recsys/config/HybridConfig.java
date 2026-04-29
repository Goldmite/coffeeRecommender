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
    // growth weight - higher number makes faster switch to cf
    private float steepness = 0.5f;
    // number of interactions for a user where the cf weight is exactly half of the
    // max cf weight
    private int inflectionPoint = 10;
    // Sensitivity to filters (user-intent)
    private float sensitivity = 0.25f;
}
