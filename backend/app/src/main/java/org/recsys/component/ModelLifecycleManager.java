package org.recsys.component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.recsys.dto.recommendation.PreparedTrainingData;
import org.recsys.dto.recommendation.TrainedModel;
import org.recsys.dto.recommendation.TrainingResult;
import org.recsys.service.MatrixFactorizationModel;
import org.recsys.service.ModelProvider;
import org.recsys.service.TrainingDataService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ModelLifecycleManager {

    private final TrainingDataService trainingDataService;
    private final MatrixFactorizationModel mfModel;
    private final ModelProvider modelProvider;

    @EventListener(ApplicationReadyEvent.class)
    public void initializeModel() {
        log.info("Checking for existing recommendation model...");

        Instant createdAt = modelProvider.refreshModel();
        Optional<TrainedModel> existingModel = modelProvider.getCurrentModel();

        if (existingModel.isPresent() && isModelFresh(createdAt)) {
            log.info("Fresh model found.");
        } else {
            log.info("No recent model found or model is stale. Triggering training...");
            // prepare training data
            PreparedTrainingData data = trainingDataService.prepareData(true);
            // train model
            TrainingResult result = mfModel.train(data);
            // save model to database as artifact
            mfModel.saveModel(result);
            // update in-memory model
            modelProvider.refreshModel();
            log.info("Model training and refresh complete.");
        }
    }

    private boolean isModelFresh(Instant createdAt) {
        long ageInHours = ChronoUnit.HOURS.between(createdAt, Instant.now());
        return ageInHours < 24;
    }
}
