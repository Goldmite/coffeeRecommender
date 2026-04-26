package org.recsys.dto.recommendation;

import org.recsys.dto.coffee.CoffeeBeanResponse;

public record RecommendationDto(
                float score,
                CoffeeBeanResponse coffee) {
}
