package org.recsys.dto.coffee;

import java.time.Instant;

public record PurchasedCoffeeDto(
        Integer rating,
        Instant purchaseDate,
        CoffeeBeanResponse coffee) {
}
