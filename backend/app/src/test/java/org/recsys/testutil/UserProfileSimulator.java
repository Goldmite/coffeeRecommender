package org.recsys.testutil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.recsys.dto.user.InteractionRequest;
import org.recsys.dto.user.OnboardingRequest;
import org.recsys.model.CoffeeBean;
import org.recsys.model.ExperienceLevel;
import org.recsys.model.PrepMethod;
import org.recsys.model.User;
import org.recsys.model.UserPreferences;
import org.recsys.repository.CoffeeRepository;
import org.recsys.repository.UserPreferencesRepository;
import org.recsys.repository.UserRepository;
import org.recsys.service.UserInteractionsService;
import org.recsys.service.UserPreferencesService;
import org.springframework.boot.test.context.TestComponent;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@TestComponent
@RequiredArgsConstructor
public class UserProfileSimulator {

    private final UserRepository userRepository;
    private final UserPreferencesRepository preferencesRepository;
    private final UserInteractionsService interactionsService;
    private final UserPreferencesService userPreferencesService;
    private final CoffeeRepository coffeeRepository;

    @Transactional
    public List<Long> generateFactorialTestProfiles() {
        List<Long> testUserIds = new ArrayList<>();
        List<CoffeeBean> allCoffees = coffeeRepository.findAll();
        Random random = new Random();

        for (ExperienceLevel level : ExperienceLevel.values()) {
            for (PrepMethod method : PrepMethod.values()) {

                User user = new User();
                String profileName = level.name() + "_" + method.name();
                user.setName("Tester " + profileName);
                user.setEmail(profileName.toLowerCase() + "@test.com");
                user.setPasswordHash("testpwhash");
                user = userRepository.save(user);
                testUserIds.add(user.getId());

                UserPreferences prefs = UserPreferences.builder()
                        .user(user)
                        .experienceLevel(level)
                        .prepMethod(method)
                        .build();
                preferencesRepository.save(prefs);

                OnboardingRequest req = new OnboardingRequest(user.getId(), level, method);
                userPreferencesService.updateUserPreferencesAfterOnboarding(req);

                int interactionCount = 10 + random.nextInt(11);
                simulateProfileChange(user.getId(), allCoffees, interactionCount, random);
            }
        }
        return testUserIds;
    }

    private void simulateProfileChange(Long userId, List<CoffeeBean> coffees, int count, Random random) {
        UserPreferences currentPrefs = preferencesRepository.findById(userId).get();
        float[] initialVector = currentPrefs.getTasteProfile();

        List<CoffeeBean> candidates = coffees.stream()
                .sorted((c1, c2) -> Double.compare(
                        calculateDistance(initialVector, c1.getFeatures().getFlavorVector()),
                        calculateDistance(initialVector, c2.getFeatures().getFlavorVector())))
                .limit(30)
                .collect(Collectors.toList());

        Collections.shuffle(candidates);

        for (int i = 0; i < count; i++) {
            if (i >= candidates.size())
                break;
            CoffeeBean coffee = candidates.get(i);

            Integer rating = null;
            Boolean purchased = false;

            double rand = random.nextDouble();
            if (rand < 0.4) {
                purchased = true;
            } else {
                purchased = true;
                rating = 4 + random.nextInt(2);
            }

            InteractionRequest interRequest = new InteractionRequest(
                    userId,
                    coffee.getId(),
                    purchased,
                    rating);

            interactionsService.addInteraction(interRequest);
        }
    }

    private double calculateDistance(float[] v1, float[] v2) {
        double sum = 0;
        for (int i = 0; i < v1.length; i++) {
            sum += Math.pow(v1[i] - v2[i], 2);
        }
        return Math.sqrt(sum);
    }
}
