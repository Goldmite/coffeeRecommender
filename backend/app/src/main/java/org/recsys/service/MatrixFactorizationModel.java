package org.recsys.service;

import java.util.List;
import java.util.Random;

import org.recsys.config.MatrixFactorizationConfig;
import org.recsys.dto.recommendation.PreparedTrainingData;
import org.recsys.dto.recommendation.RatingTriplet;
import org.recsys.dto.recommendation.TrainedModel;
import org.recsys.dto.recommendation.TrainingResult;
import org.recsys.model.ModelMetadata;
import org.recsys.model.TrainedModelArtifact;
import org.recsys.repository.TrainedModelRepository;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MatrixFactorizationModel {

    private final MatrixFactorizationConfig config;
    private final TrainedModelRepository repository;

    public TrainingResult train(PreparedTrainingData data) {
        return train(data, config.getLearningRate(), config.getRegularization(), config.getLatentFactors(),
                config.getEpochs());
    }

    public TrainingResult train(PreparedTrainingData data, float learningRate, float regularization, int K,
            int epochs) {
        int userAmount = data.userMapper().getSize();
        int coffeeAmount = data.coffeeMapper().getSize();
        float mean = data.globalMean();
        List<RatingTriplet> triplets = data.triplets();

        float[] userFactors = new float[userAmount * K];
        float[] coffeeFactors = new float[coffeeAmount * K];
        float[] userBiases = new float[userAmount];
        float[] coffeeBiases = new float[coffeeAmount];

        float[] userAlphas = new float[userAmount];
        int binAmount = 6; // 2 years / 4 months
        float[] coffeeBinBiases = new float[coffeeAmount * binAmount];
        long binPeriodSeconds = 10368000; // ~ 4 months

        Random rand = new Random();
        for (int i = 0; i < userFactors.length; i++)
            userFactors[i] = (float) (rand.nextGaussian() * 0.1);
        for (int i = 0; i < coffeeFactors.length; i++)
            coffeeFactors[i] = (float) (rand.nextGaussian() * 0.1);

        double rmse = 0;

        for (int epoch = 0; epoch < epochs; epoch++) {
            double totalError = 0;

            for (RatingTriplet triplet : triplets) {
                int u = triplet.userIndex();
                int i = triplet.coffeeIndex();
                int uOffset = u * K;
                int iOffset = i * K;
                // coffee periodical bins (discrete)
                int binIdx = (int) ((triplet.timestamp() - data.minTimestamp()) / binPeriodSeconds);
                binIdx = Math.min(binIdx, binAmount - 1); // cap at most recent period
                int bOffset = i * binAmount + binIdx;

                float dotProduct = 0; // Pu · Qi
                for (int k = 0; k < K; k++) {
                    dotProduct += userFactors[uOffset + k] * coffeeFactors[iOffset + k];
                }
                // PREDICTION (Score): Rui = μ + Bu(t) + (Bi + Bi_bin) + (Pu · Qi)
                float prediction = mean + (userBiases[u] + userAlphas[u] * triplet.dev())
                        + (coffeeBiases[i] + coffeeBinBiases[bOffset]) + dotProduct;

                float error = triplet.score() - prediction;
                totalError += (error * error); // square error
                // update biases
                // Bu += gamma * (e - lambda * Bu)
                userBiases[u] += learningRate * (error - regularization * userBiases[u]);
                // Bi += gamma * (e - lambda * Bi)
                coffeeBiases[i] += learningRate * (error - regularization * coffeeBiases[i]);
                // update temporal data
                // Au += gamma * (e * devU(t) - lambda * Au)
                userAlphas[u] += learningRate * (error * triplet.dev() - regularization * userAlphas[u]);
                // Bi_bin += gamma * (e - lamdba * Bi_bin)
                coffeeBinBiases[i] += learningRate * (error - regularization * coffeeBinBiases[i]);
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
            rmse = Math.sqrt(totalError / triplets.size());
            System.out.printf("Epoch %d/%d - RMSE: %.4f%n", epoch + 1, epochs, rmse);
        }
        TrainedModel model = new TrainedModel(userFactors, coffeeFactors, userBiases, coffeeBiases, userAlphas,
                coffeeBinBiases, K, mean,
                data.userMapper(),
                data.coffeeMapper());
        return new TrainingResult(model, rmse);
    }

    @Transactional
    public void saveModel(TrainingResult result) {
        repository.deactivateOldModels();

        TrainedModel model = result.model();

        ModelMetadata metadata = ModelMetadata.builder()
                .k(model.K())
                .gamma(config.getLearningRate())
                .lambda(config.getRegularization())
                .epochs(config.getEpochs())
                .userCount(model.userMapper().getSize())
                .coffeeCount(model.coffeeMapper().getSize())
                .build();

        byte[] serializedModel = model.serialize();
        TrainedModelArtifact artifact = TrainedModelArtifact.builder()
                .version(repository.findMaxVersion() + 1)
                .data(serializedModel)
                .rmse(result.rmse())
                .isActive(true)
                .metadata(metadata)
                .build();
        repository.save(artifact);
    }
}
