package org.recsys.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.recsys.dto.recommendation.PreparedTrainingData;
import org.recsys.dto.recommendation.RatingTriplet;
import org.recsys.mapper.IndexMapper;
import org.recsys.model.UserInteractions;
import org.recsys.repository.UserInteractionsRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TrainingDataService {

    private final UserInteractionsRepository interactionRepository;

    public List<UserInteractions> getAllUserInteractions() {
        return interactionRepository.findAll();
    }

    public PreparedTrainingData prepareData(boolean shuffled) {
        List<UserInteractions> interactions = getAllUserInteractions();

        IndexMapper userMapper = new IndexMapper();
        IndexMapper coffeeMapper = new IndexMapper();
        List<RatingTriplet> triplets = new ArrayList<>();

        Map<Integer, Double> userTimeSums = new HashMap<>();
        Map<Integer, Integer> userCounts = new HashMap<>();
        double totalRatingSum = 0;

        for (UserInteractions ui : interactions) {
            // 1. Get/Create indices
            int uIdx = userMapper.getInternalIndex(ui.getUserId());
            int iIdx = coffeeMapper.getInternalIndex(ui.getCoffeeId());
            // 2. Determine the score
            float score = calculateScore(ui);
            totalRatingSum += score;
            // 3. Prepare time mean data
            long timestamp = ui.getCreatedAt().getEpochSecond();
            userTimeSums.put(uIdx, userTimeSums.getOrDefault(uIdx, 0.0) + timestamp);
            userCounts.put(uIdx, userCounts.getOrDefault(uIdx, 0) + 1);
            // 4. Add to the list
            triplets.add(new RatingTriplet(uIdx, iIdx, score, timestamp));
        }

        float[] userMeanTimestamps = new float[userMapper.getSize()];
        for (int u = 0; u < userMapper.getSize(); u++) {
            userMeanTimestamps[u] = (float) (userTimeSums.get(u) / userCounts.get(u));
        }

        float globalMean = triplets.isEmpty() ? 0 : (float) (totalRatingSum / triplets.size());

        if (shuffled)
            Collections.shuffle(triplets);

        return new PreparedTrainingData(triplets, userMapper, coffeeMapper, globalMean, userMeanTimestamps);
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
