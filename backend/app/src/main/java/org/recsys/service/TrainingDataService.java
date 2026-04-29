package org.recsys.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.recsys.config.MatrixFactorizationConfig;
import org.recsys.dto.recommendation.PreparedTrainingData;
import org.recsys.dto.recommendation.RatingTriplet;
import org.recsys.mapper.IndexMapper;
import org.recsys.model.UserInteractions;
import org.recsys.repository.UserInteractionsRepository;
import org.recsys.util.PredictionUtils;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TrainingDataService {

    private final MatrixFactorizationConfig config;
    private final UserInteractionsRepository interactionRepository;

    public List<UserInteractions> getAllUserInteractions() {
        return interactionRepository.findAll();
    }

    public Optional<PreparedTrainingData> prepareData(boolean shuffled) {
        List<UserInteractions> interactions = getAllUserInteractions();
        if (interactions.isEmpty())
            return Optional.empty();

        IndexMapper userMapper = new IndexMapper();
        IndexMapper coffeeMapper = new IndexMapper();
        List<RatingTriplet> triplets = new ArrayList<>();

        Map<Integer, Double> userTimeSums = new HashMap<>();
        Map<Integer, Integer> userCounts = new HashMap<>();

        long minTimestamp = Long.MAX_VALUE;
        double totalRatingSum = 0;

        for (UserInteractions ui : interactions) {
            // 1. Get/Create indices
            int uIdx = userMapper.getInternalIndex(ui.getUserId());
            // 3. Prepare time mean data
            long timestamp = ui.getCreatedAt().getEpochSecond();
            if (timestamp < minTimestamp)
                minTimestamp = timestamp;

            userTimeSums.put(uIdx, userTimeSums.getOrDefault(uIdx, 0.0) + timestamp);
            userCounts.put(uIdx, userCounts.getOrDefault(uIdx, 0) + 1);
        }

        int userAmount = userMapper.getSize();
        float[] userTimestampMeans = new float[userAmount];
        for (int u = 0; u < userAmount; u++) {
            userTimestampMeans[u] = (float) (userTimeSums.get(u) / userCounts.get(u));
        }

        // add triplets with precalculated dev
        for (UserInteractions ui : interactions) {
            int uIdx = userMapper.getInternalIndex(ui.getUserId());
            int iIdx = coffeeMapper.getInternalIndex(ui.getCoffeeId());
            long t = ui.getCreatedAt().getEpochSecond();
            // User deviation over time (continuous)
            float dev = PredictionUtils.calculateUserDev(t, userTimestampMeans[uIdx], config.getBeta());
            // Determine the score
            float score = calculateScore(ui);
            totalRatingSum += score;

            triplets.add(new RatingTriplet(uIdx, iIdx, score, t, dev));
        }

        float globalMean = triplets.isEmpty() ? 0 : (float) (totalRatingSum / triplets.size());

        if (shuffled)
            Collections.shuffle(triplets);

        return Optional.of(new PreparedTrainingData(triplets, userMapper, coffeeMapper, globalMean, minTimestamp,
                userTimestampMeans));
    }

    private float calculateScore(UserInteractions ui) {
        if (ui.getRating() != null && ui.getRating() > 0)
            return ui.getRating().floatValue();
        if (ui.getIsPurchased())
            return 3.0f;
        if (ui.getIsClicked())
            return 1.7f;
        return 0.0f;
    }
}
