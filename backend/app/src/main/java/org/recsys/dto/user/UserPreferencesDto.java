package org.recsys.dto.user;

public record UserPreferencesDto(
        Long userId,
        String experienceLevel,
        String prepMethod) {
}
