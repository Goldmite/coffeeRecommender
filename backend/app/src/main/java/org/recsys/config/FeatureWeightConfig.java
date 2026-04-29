package org.recsys.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "recommendation.weights")
@Getter
@Setter
public class FeatureWeightConfig {
    // Attributes
    private float roastLevel = 1.5f;
    private float scaScore = 1.0f;
    private float altitude = 0.5f;
    private float acidity = 1.5f;
    private float body = 1.0f;
    private float aftertaste = 1.0f;
    private float sweetness = 1.5f;
    private float bitterness = 1.0f;
    private float singleOrigin = 1.0f;

    // Flavor Categories (Highly essential)
    private float fruity = 2.0f;
    private float floral = 2.0f;
    private float nuttyCocoa = 2.0f;
    private float sweet = 2.0f;
    private float spices = 1.5f;
    private float vegetal = 1.5f;
    private float sour = 1.5f;

    // Origins/Process
    private float processing = 1.0f;
    private float origins = 0.5f;
}