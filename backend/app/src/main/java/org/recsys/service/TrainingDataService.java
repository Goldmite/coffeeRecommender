package org.recsys.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

        double totalRatingSum = 0;

        for (UserInteractions ui : interactions) {
            // 1. Get/Create indices
            int uIdx = userMapper.getInternalIndex(ui.getUserId());
            int iIdx = coffeeMapper.getInternalIndex(ui.getCoffeeId());
            // 2. Determine the score
            float score = calculateScore(ui);
            totalRatingSum += score;
            // 3. Add to the list
            long timestamp = ui.getCreatedAt().getEpochSecond();
            triplets.add(new RatingTriplet(uIdx, iIdx, score, timestamp));
        }

        float globalMean = triplets.isEmpty() ? 0 : (float) (totalRatingSum / triplets.size());

        if (shuffled)
            Collections.shuffle(triplets);

        return new PreparedTrainingData(triplets, userMapper, coffeeMapper, globalMean);
    }

    private float calculateScore(UserInteractions ui) {
        if (ui.getRating() != null && ui.getRating() > 0)
            return ui.getRating().floatValue();
        if (ui.getIsPurchased())
            return 5.0f;
        if (ui.getIsClicked())
            return 2.0f;
        return 0.0f;
    }
}
