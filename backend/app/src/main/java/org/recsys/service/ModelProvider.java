package org.recsys.service;

import java.util.Optional;

import org.recsys.dto.recommendation.TrainedModel;
import org.recsys.repository.TrainedModelRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ModelProvider {

    private TrainedModel currentModel;
    private final TrainedModelRepository repository;

    public Optional<TrainedModel> getCurrentModel() {
        return Optional.ofNullable(currentModel);
    }

    public void refreshModel() {
        repository.findByIsActiveTrue().ifPresentOrElse(entity -> {
            try {
                TrainedModel model = TrainedModel.deserialize(entity.getData());
                // update in-memory model
                this.currentModel = model;
                log.info("Model refreshed to version: {} with RMSE: {}", entity.getVersion(), entity.getRmse());
            } catch (Exception e) {
                log.error("Failed to deserialize the model", e);
            }
        }, () -> log.warn("No active model found in database. Recommendations will use fallback logic."));
    }

}
