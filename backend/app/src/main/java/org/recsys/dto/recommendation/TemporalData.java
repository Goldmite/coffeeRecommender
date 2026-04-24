package org.recsys.dto.recommendation;

public record TemporalData(
        float minTimestamp,
        float[] deviations) {
}
