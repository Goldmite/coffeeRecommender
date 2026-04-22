package org.recsys.service;

import java.util.Optional;

import org.recsys.model.ExperienceLevel;
import org.recsys.model.User;
import org.recsys.model.UserPreferences;
import org.recsys.repository.UserPreferencesRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserPreferencesService {

    private final UserPreferencesRepository preferencesRepository;
    private final CoffeeVectorService vectorService;

    public UserPreferences setDefaultPreferencesForUser(User user) {
        UserPreferences preferences = UserPreferences.builder()
                .user(user)
                .experienceLevel(ExperienceLevel.BEGINNER)
                .prepMethod(null)
                .tasteProfile(vectorService.createBaseVector())
                .build();

        return preferencesRepository.save(preferences);
    }

    public Optional<UserPreferences> getUserPreferencesByUserId(Long userId) {
        return preferencesRepository.findById(userId);
    }
}
