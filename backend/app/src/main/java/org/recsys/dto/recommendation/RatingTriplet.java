package org.recsys.dto.recommendation;

public record RatingTriplet(
                int userIndex,
                int coffeeIndex,
                float score,
                long timestamp,
                float dev) {
}
