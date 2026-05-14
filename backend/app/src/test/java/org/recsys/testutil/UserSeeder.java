package org.recsys.testutil;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.recsys.dto.user.OnboardingRequest;
import org.recsys.model.CoffeeBean;
import org.recsys.model.ExperienceLevel;
import org.recsys.model.PrepMethod;
import org.recsys.model.User;
import org.recsys.model.UserInteractions;
import org.recsys.model.UserPreferences;
import org.recsys.repository.CoffeeRepository;
import org.recsys.repository.UserInteractionsRepository;
import org.recsys.repository.UserPreferencesRepository;
import org.recsys.repository.UserRepository;
import org.recsys.service.UserPreferencesService;
import org.springframework.boot.test.context.TestComponent;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@TestComponent
@RequiredArgsConstructor
public class UserSeeder {

    private final UserRepository userRepository;
    private final UserPreferencesRepository preferencesRepository;
    private final UserInteractionsRepository interactionsRepository;
    private final UserPreferencesService userPreferencesService;
    private final CoffeeRepository coffeeRepository;

    @Transactional
    public void seedCommunity(int userCount) {
        List<CoffeeBean> allCoffees = coffeeRepository.findAll();
        if (allCoffees.isEmpty())
            throw new IllegalStateException();

        Random random = new Random();

        for (int i = 1; i <= userCount; i++) {
            User user = new User();
            user.setName("Test User " + i);
            user.setEmail("user" + i + "@tester.com");
            user.setPasswordHash("hashtestpw");
            user = userRepository.save(user);

            ExperienceLevel xp = ExperienceLevel.values()[random.nextInt(ExperienceLevel.values().length)];
            PrepMethod method = PrepMethod.values()[random.nextInt(PrepMethod.values().length)];
            UserPreferences prefs = UserPreferences.builder()
                    .user(user)
                    .experienceLevel(xp)
                    .prepMethod(method)
                    .build();
            preferencesRepository.save(prefs);

            OnboardingRequest req = new OnboardingRequest(user.getId(), xp, method);
            userPreferencesService.updateUserPreferencesAfterOnboarding(req);
            UserPreferences updatedPrefs = preferencesRepository.findById(user.getId()).get();

            int interactionCount = 5 + random.nextInt(11);
            generateLogicalInteractions(updatedPrefs, allCoffees, interactionCount, random);
        }
    }

    private void generateLogicalInteractions(UserPreferences prefs, List<CoffeeBean> coffees, int count,
            Random random) {
        Collections.shuffle(coffees);

        for (int i = 0; i < count; i++) {
            CoffeeBean coffee = coffees.get(i);

            double similarity = calculateEuclideanSimilarity(prefs.getTasteProfile(),
                    coffee.getFeatures().getFlavorVector());

            int rating = mapSimilarityToRating(similarity, random);

            UserInteractions interaction = UserInteractions.builder()
                    .userId(prefs.getUserId())
                    .coffeeId(coffee.getId())
                    .rating(rating)
                    .isPurchased(rating >= 4)
                    .isClicked(true)
                    .purchaseDate(Instant.now().minus(random.nextInt(30), ChronoUnit.DAYS))
                    .build();

            interactionsRepository.save(interaction);
        }
    }

    private double calculateEuclideanSimilarity(float[] v1, float[] v2) {
        double sum = 0;
        for (int i = 0; i < v1.length; i++) {
            sum += Math.pow(v1[i] - v2[i], 2);
        }
        return 1.0 / (1.0 + Math.sqrt(sum));
    }

    private int mapSimilarityToRating(double similarity, Random random) {
        double noise = random.nextGaussian() * 0.1;
        double finalScore = similarity + noise;

        if (finalScore > 0.7)
            return 5;
        if (finalScore > 0.5)
            return 4;
        if (finalScore > 0.3)
            return 3;
        if (finalScore > 0.2)
            return 2;
        return 1;
    }
}
