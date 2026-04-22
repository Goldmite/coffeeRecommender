package org.recsys.dto.recommendation;

import org.recsys.dto.coffee.CoffeeBeanResponse;

public record CoffeeRecommendationResponse(CoffeeBeanResponse coffee, Double similarity) {
}
