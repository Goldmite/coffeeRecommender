package org.recsys.service;

import java.util.List;
import java.util.Random;

import org.recsys.config.MatrixFactorizationConfig;
import org.recsys.dto.recommendation.PreparedTrainingData;
import org.recsys.dto.recommendation.RatingTriplet;
import org.recsys.dto.recommendation.TrainedModel;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MatrixFactorizationModel {

    private final MatrixFactorizationConfig config;

    public TrainedModel train(PreparedTrainingData data) {
        return train(data, config.getLearningRate(), config.getRegularization(), config.getLatentFactors(),
                config.getEpochs());
    }

    public TrainedModel train(PreparedTrainingData data, float learningRate, float regularization, int K, int epochs) {
        int userAmount = data.userMapper().getSize();
        int coffeeAmount = data.coffeeMapper().getSize();
        float mean = data.globalMean();
        List<RatingTriplet> triplets = data.triplets();

        float[] userFactors = new float[userAmount * K];
        float[] coffeeFactors = new float[coffeeAmount * K];
        float[] userBiases = new float[userAmount];
        float[] coffeeBiases = new float[coffeeAmount];

        Random rand = new Random();
        for (int i = 0; i < userFactors.length; i++)
            userFactors[i] = (float) (rand.nextGaussian() * 0.1);
        for (int i = 0; i < coffeeFactors.length; i++)
            coffeeFactors[i] = (float) (rand.nextGaussian() * 0.1);

        for (int epoch = 0; epoch < epochs; epoch++) {
            double totalError = 0;

            for (RatingTriplet triplet : triplets) {
                int u = triplet.userIndex();
                int i = triplet.coffeeIndex();
                int uOffset = u * K;
                int iOffset = i * K;
                float dotProcuct = 0; // Pu · Qi
                for (int k = 0; k < K; k++) {
                    dotProcuct += userFactors[uOffset + k] * coffeeFactors[iOffset + k];
                }
                // (Score) Rui = μ + Bu + Bi + (Pu · Qi)
                float prediction = mean + userBiases[u] + coffeeBiases[i] + dotProcuct;
                float error = triplet.score() - prediction;
                totalError += (error * error); // square error
                // update biases:
                // Bu += gamma * (e - lambda * Bu)
                userBiases[u] += learningRate * (error - regularization * userBiases[u]);
                // Bi += gamma * (e - lambda * Bi)
                coffeeBiases[i] += learningRate * (error - regularization * coffeeBiases[i]);
                // update latent factors
                for (int k = 0; k < K; k++) {
                    float p_uk = userFactors[uOffset + k];
                    float q_ik = coffeeFactors[iOffset + k];
                    // Pu += gamma * (Eui * Qi - lambda * Pu)
                    userFactors[uOffset + k] += learningRate * (error * q_ik - regularization * p_uk);
                    // Qi += gamma * (Eui * Pu - lambda * Qi)
                    coffeeFactors[iOffset + k] += learningRate * (error * p_uk - regularization * q_ik);
                }
            }
            // root mean square error
            double rmse = Math.sqrt(totalError / triplets.size());
            System.out.printf("Epoch %d/%d - RMSE: %.4f%n", epoch + 1, epochs, rmse);
        }
        return new TrainedModel(userFactors, coffeeFactors, userBiases, coffeeBiases, K, data.userMapper(),
                data.coffeeMapper());
    }
}
