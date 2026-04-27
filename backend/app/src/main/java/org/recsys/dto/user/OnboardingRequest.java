package org.recsys.dto.user;

import org.recsys.model.ExperienceLevel;
import org.recsys.model.PrepMethod;

public record OnboardingRequest(
        Long userId,
        ExperienceLevel experience,
        PrepMethod prepMethod) {
}
