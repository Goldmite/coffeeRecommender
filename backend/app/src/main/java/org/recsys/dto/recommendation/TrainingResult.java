package org.recsys.dto.recommendation;

public record TrainingResult(
                TrainedModel model,
                double rmse) {
}
