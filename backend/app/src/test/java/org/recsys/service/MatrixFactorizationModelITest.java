package org.recsys.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.recsys.config.TestConfig;
import org.recsys.dto.recommendation.PreparedTrainingData;
import org.recsys.dto.recommendation.TrainedModel;
import org.recsys.model.CoffeeBean;
import org.recsys.model.User;
import org.recsys.model.UserInteractions;
import org.recsys.repository.UserInteractionsRepository;
import org.recsys.repository.UserRepository;
import org.recsys.testutil.TestDataFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
class MatrixFactorizationModelITest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TrainingDataService trainingDataService;

    @Autowired
    private MatrixFactorizationModel mfModel;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserInteractionsRepository interactionRepository;

    private User savedUser1;
    private User savedUser2;

    @BeforeEach
    void setup() {
        // 1. Clear everything in reverse order of dependency
        interactionRepository.deleteAll();
        userRepository.deleteAll();
        // 2. Create and Save real Users
        savedUser1 = userRepository.save(TestDataFactory.createUser("user1@test.com", "hash"));
        savedUser2 = userRepository.save(TestDataFactory.createUser("user2@test.com", "hash"));
        // 3. Create and Save Interactions linking the real entities
        interactionRepository.saveAll(List.of(
                createInteraction(savedUser1, 10L, 5), // User 1 likes Coffee A
                createInteraction(savedUser1, 20L, 2), // User 1 dislikes Coffee B
                createInteraction(savedUser2, 20L, 5) // User 2 likes Coffee B
        ));
    }

    @Test
    void modelShouldPredictHigherScoresForLikedCoffees() {
        // 1. Arrange
        PreparedTrainingData data = trainingDataService.prepareData(true);
        // 2. Act: Train with enough epochs to allow convergence
        // We use a slightly higher epoch count (100) to ensure the error drops
        TrainedModel model = mfModel.train(data, 0.02f, 0.02f, 25, 50);
        // 3. Assert: Verify the predictions reflect the training data
        float predictionLike = model.predict(savedUser1.getId(), 10L, data.globalMean());
        float predictionDislike = model.predict(savedUser1.getId(), 20L, data.globalMean());
        float predictionUser2 = model.predict(savedUser2.getId(), 20L, data.globalMean());
        // User 1's prediction for Coffee 10 should be significantly higher than for
        // Coffee 20
        assertTrue(predictionLike > predictionDislike,
                "Model should predict a higher score for liked coffee (10) than disliked coffee (20)");
        // User 1's prediction for Coffee 10 should be near the original rating (5.0)
        // We allow a margin (delta) because MF is an approximation
        assertEquals(5.0f, predictionLike, 1.5f);
        // Check cross-user consistency
        assertTrue(predictionUser2 > predictionDislike,
                "Coffee 20 should have a high prediction for User 2 despite User 1 hating it");
    }

    private UserInteractions createInteraction(User user, Long coffeeId, Integer rating) {
        CoffeeBean bean = entityManager.getReference(CoffeeBean.class, coffeeId);
        UserInteractions ui = UserInteractions.builder()
                .user(user)
                .coffeeBean(bean)
                .userId(user.getId())
                .coffeeId(coffeeId)
                .rating(rating)
                .isClicked(true)
                .isPurchased(rating != null && rating > 3)
                .createdAt(Instant.now())
                .build();
        return ui;
    }
}
