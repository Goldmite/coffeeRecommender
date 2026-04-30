package org.recsys.dto.user;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record InteractionRequest(
        @NotNull Long userId,
        @NotNull Long coffeeId,
        Boolean purchased,
        @Min(1) @Max(5) Integer rating) {
}
