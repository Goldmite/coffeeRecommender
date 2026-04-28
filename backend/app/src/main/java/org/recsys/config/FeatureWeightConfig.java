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
    private float roastLevel = 1.0f;
    private float scaScore = 1.5f;
    private float altitude = 1.0f;
    private float acidity = 1.4f;
    private float body = 1.0f;
    private float aftertaste = 1.0f;
    private float sweetness = 1.2f;
    private float bitterness = 1.0f;
    private float singleOrigin = 1.0f;

    // Flavor Categories (Highly essential)
    private float fruity = 2.0f;
    private float floral = 2.0f;
    private float nuttyCocoa = 1.5f;
    private float sweet = 2.0f;
    private float spices = 1.5f;
    private float vegetal = 1.0f;
    private float sour = 1.0f;

    // Origins/Process
    private float processing = 1.0f;
    private float origins = 1.0f;
}