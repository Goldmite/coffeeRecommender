package org.recsys.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "recommendation.mf")
@Getter
@Setter
public class MatrixFactorizationConfig {
    /**
     * Number of latent factors (K).
     * Higher values can capture more complex patterns but risk overfitting.
     */
    private int latentFactors = 50;

    /**
     * The learning rate (Gamma).
     * Controls the step size during gradient descent. Usually 0.001 to 0.01.
     */
    private float learningRate = 0.02f;

    /**
     * Regularization parameter (Lambda).
     * Penalizes large weights to prevent the model from memorizing the training
     * data.
     */
    private float regularization = 0.03f;

    /**
     * Number of training passes over the entire dataset.
     */
    private int epochs = 70;

    /**
     * A constant power to control stretch of time
     */
    private double beta = 0.4;
}
