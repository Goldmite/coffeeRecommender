package org.recsys.service;

import java.util.Optional;

import org.recsys.dto.coffee.CoffeeVectorizationDto;
import org.recsys.dto.user.OnboardingRequest;
import org.recsys.model.ExperienceLevel;
import org.recsys.model.Processing;
import org.recsys.model.RoastLevel;
import org.recsys.model.User;
import org.recsys.model.UserPreferences;
import org.recsys.repository.UserPreferencesRepository;
import org.springframework.stereotype.Service;

import io.hypersistence.utils.hibernate.type.range.Range;
import jakarta.persistence.EntityNotFoundException;
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
                .tasteProfile(null)
                .build();

        return preferencesRepository.save(preferences);
    }

    public Optional<UserPreferences> getUserPreferencesByUserId(Long userId) {
        return preferencesRepository.findById(userId);
    }

    public float[] getUserPreferenceFlavorProfile(Long userId) {
        Optional<UserPreferences> pref = getUserPreferencesByUserId(userId);
        if (pref.isPresent() && pref.get().getTasteProfile() != null) {
            return pref.get().getTasteProfile();
        } else {
            return vectorService.createBaseVector(); // fallback
        }
    }

    public void updateUserPreferencesAfterOnboarding(OnboardingRequest request) {
        UserPreferences pref = preferencesRepository.findById(request.userId())
                .orElseThrow(() -> new EntityNotFoundException());

        pref.setExperienceLevel(request.experience());
        pref.setPrepMethod(request.prepMethod());

        float[] adjustedFlavorProfile = calculateNewUserVector(request);
        pref.setTasteProfile(adjustedFlavorProfile);

        preferencesRepository.save(pref);
    }

    public boolean isUserUsingDefaultPreferences(Long userId) {
        return preferencesRepository.isNewUser(userId);
    }

    public float[] calculateNewUserVector(OnboardingRequest request) {
        int acidity = 5;
        int body = 5;
        int aftertaste = 5;
        int bitterness = 5;
        int sweetness = 6;
        double scaScore = 80.0;
        int roast = RoastLevel.MEDIUM.ordinal();
        String process = Processing.NATURAL.getProcess();
        // 1. Adjust based on Prep Method (The "Extraction" Bias)
        switch (request.prepMethod()) {
            case ESPRESSO -> {
                body += 3; // Espresso is intense
                bitterness += 2; // Tolerance for higher punch
                roast = RoastLevel.DARK.ordinal();
            }
            case POUROVER -> {
                acidity += 2; // Filter coffee highlights brightness
                body -= 2; // Body is lighter/cleaner
                roast = RoastLevel.MEDIUM_LIGHT.ordinal();
            }
            case IMMERSION -> {
                // French Press / Cup infusion: Balanced but heavy
                body += 1;
                bitterness += 1;
                roast = RoastLevel.MEDIUM_DARK.ordinal();
            }
            case COLD_BREW -> {
                sweetness += 2; // Cold brew is naturally sweeter/less acidic
                acidity -= 3;
                body += 1;
            }
            case OTHER -> {
                // no adjustments
            }
        }

        // 2. Adjust based on Experience (The "Complexity" Bias)
        switch (request.experience()) {
            case BEGINNER -> {
                // Beginners usually prefer lower acidity and more "traditional" flavors
                acidity -= 2;
                bitterness += 1;
                body += 2;
            }
            case INTERMEDIATE -> {
                aftertaste += 1;
                sweetness += 1;
            }
            case ADVANCED -> {
                acidity += 1;
                aftertaste += 2;
                body -= 1;
                sweetness += 2;
                scaScore = 83.5;
                process = Processing.WASHED.getProcess();
            }
            case EXPERT -> {
                // Experts usually seek high acidity (vibrancy) and complexity
                acidity += 3;
                sweetness += 2;
                bitterness -= 1;
                aftertaste += 2;
                scaScore = 87.0;
                process = Processing.WASHED.getProcess();
            }
        }

        CoffeeVectorizationDto dto = CoffeeVectorizationDto.builder()
                .process(process)
                .roastLevel(roast)
                .scaScore(scaScore)
                .altitude(determineAltitude(request.experience()))
                .acidity(clamp(acidity))
                .body(clamp(body))
                .aftertaste(aftertaste)
                .sweetness(clamp(sweetness))
                .bitterness(clamp(bitterness))
                .build();

        float[] adjustedVector = vectorService.createFlavorVector(dto);

        adjustedVector[8] = 0; // origin type (single/blend)

        return adjustedVector;
    }

    private Range<Integer> determineAltitude(ExperienceLevel xp) {
        return switch (xp) {
            case EXPERT, ADVANCED -> Range.closed(1600, 2500);
            default -> Range.closed(1000, 1900);
        };
    }

    private int clamp(int val) {
        return Math.max(1, Math.min(10, val));
    }
}
