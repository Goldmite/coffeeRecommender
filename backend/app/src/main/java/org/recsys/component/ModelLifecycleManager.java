package org.recsys.component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.recsys.dto.recommendation.PreparedTrainingData;
import org.recsys.dto.recommendation.TrainedModel;
import org.recsys.dto.recommendation.TrainingResult;
import org.recsys.repository.CoffeeRepository;
import org.recsys.repository.UserInteractionsRepository;
import org.recsys.repository.UserRepository;
import org.recsys.service.MatrixFactorizationModel;
import org.recsys.service.ModelProvider;
import org.recsys.service.TrainingDataService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ModelLifecycleManager {

    private final TrainingDataService trainingDataService;
    private final UserInteractionsRepository interactionsRepository;
    private final CoffeeRepository coffeeRepository;
    private final UserRepository userRepository;
    private final MatrixFactorizationModel mfModel;
    private final ModelProvider modelProvider;

    private float systemMaturity = 0;

    @EventListener(ApplicationReadyEvent.class)
    public void initializeModel() {
        refreshMaturity();
        log.info("Checking for existing recommendation model...");
        Instant createdAt = modelProvider.refreshModel();
        Optional<TrainedModel> existingModel = modelProvider.getCurrentModel();

        if (existingModel.isPresent() && isModelFresh(createdAt)) {
            log.info("Fresh model found.");
        } else {
            log.info("No recent model found or model is stale. Preparing data...");
            // prepare training data
            Optional<PreparedTrainingData> data = trainingDataService.prepareData(true);
            if (data.isPresent()) {
                log.info("Data prepared. Initiating training...");
                TrainingResult result = mfModel.train(data.get());
                // save model to database as artifact
                mfModel.saveModel(result);
                // update in-memory model
                modelProvider.refreshModel();
                log.info("Model training and refresh complete.");
            } else {
                log.info("No data for training. Model was NOT created.");
            }
        }
    }

    private boolean isModelFresh(Instant createdAt) {
        long ageInHours = ChronoUnit.HOURS.between(createdAt, Instant.now());
        return ageInHours < 24;
    }

    @Scheduled(fixedRate = 3600000)
    public void refreshMaturity() {
        log.info("Refreshing system interaction maturity...");
        long totalUsers = userRepository.count();
        long totalInteractions = interactionsRepository.count();
        long totalCoffees = coffeeRepository.count();

        if (totalUsers < 5 || totalCoffees == 0) {
            this.systemMaturity = 0.0f;
            return;
        } // scale to 1.0 when enough users
        float userFactor = Math.min(1.0f, totalUsers / 25.0f);
        float avgInteractionPerUser = (float) (totalInteractions / totalUsers);
        float densityFactor = Math.min(1.0f, avgInteractionPerUser / 10.0f);

        this.systemMaturity = userFactor * densityFactor;

        log.info("System maturity updated: %f", this.systemMaturity);
    }

    public float getSystemMaturity() {
        return this.systemMaturity;
    }
}
