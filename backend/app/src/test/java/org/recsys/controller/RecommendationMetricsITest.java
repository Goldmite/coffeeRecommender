package org.recsys.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.recsys.component.ModelLifecycleManager;
import org.recsys.config.TestConfig;
import org.recsys.dto.recommendation.RecommendationDto;
import org.recsys.dto.recommendation.RecommendationFilterRequest;
import org.recsys.model.CoffeeBean;
import org.recsys.repository.CoffeeRepository;
import org.recsys.repository.UserInteractionsRepository;
import org.recsys.service.RecommenderService;
import org.recsys.testutil.UserProfileSimulator;
import org.recsys.testutil.UserSeeder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import({ TestConfig.class, UserSeeder.class, UserProfileSimulator.class })
class RecommendationMetricsITest {

    record EvaluationMetrics(
            double novelty,
            int diversity,
            double serendipity) {
    }

    @Autowired
    private RecommenderService recommendationService;
    @Autowired
    private UserInteractionsRepository interactionsRepository;
    @Autowired
    private CoffeeRepository coffeeRepository;
    @Autowired
    private UserSeeder communitySeeder;
    @Autowired
    private UserProfileSimulator profileSimulator;
    @Autowired
    private ModelLifecycleManager modelManager;

    @Test
    void evaluateRecommendationQuality() {
        // given
        communitySeeder.seedCommunity(100);
        List<Long> testUserIds = profileSimulator.generateFactorialTestProfiles();
        // init model with new data
        modelManager.initializeModel();

        List<CoffeeBean> allCoffees = coffeeRepository.findAll();
        Set<Long> totalRecommendedCoffees = new HashSet<>();
        List<EvaluationMetrics> allResults = new ArrayList<>();
        List<Long> popularCoffeeIds = interactionsRepository.findTopPopularCoffeeIds(PageRequest.of(0, 10));

        RecommendationFilterRequest emptyFilter = new RecommendationFilterRequest(null, null);
        // when
        for (Long userId : testUserIds) {
            List<RecommendationDto> recommendations = recommendationService.getHybridRecommendations(userId, 10,
                    emptyFilter);
            List<Long> coffeeIds = recommendations.stream().map(r -> r.coffee().getId()).toList();

            totalRecommendedCoffees.addAll(coffeeIds);

            long alreadyInteractedCount = interactionsRepository.countByUserIdAndCoffeeIdIn(userId, coffeeIds);
            double novelty = (10.0 - alreadyInteractedCount) / 10.0;

            long uniqueOrigins = recommendations.stream()
                    .flatMap(r -> r.coffee().getOrigins().stream())
                    .distinct()
                    .count();

            long unexpectedCount = coffeeIds.stream()
                    .filter(id -> !popularCoffeeIds.contains(id))
                    .count();
            double serendipity = unexpectedCount / 10.0;

            allResults.add(new EvaluationMetrics(novelty, (int) uniqueOrigins, serendipity));
        }
        // then
        double avgNovelty = allResults.stream().mapToDouble(m -> m.novelty).average().orElse(0);
        double avgDiversity = allResults.stream().mapToDouble(m -> m.diversity).average().orElse(0);
        double avgSerendipity = allResults.stream().mapToDouble(m -> m.serendipity).average().orElse(0);
        double coverage = (double) totalRecommendedCoffees.size() / allCoffees.size() * 100;

        printResultsTable(avgNovelty, avgDiversity, avgSerendipity, coverage);

        assertTrue(avgNovelty > 0.15);
        assertTrue(avgDiversity >= 3.0);
        assertTrue(coverage > 20.0);
    }

    private void printResultsTable(double nov, double div, double seren, double cov) {
        System.out.println("\n------- OFFLINE EVALUATION RESULTS -------");
        System.out.printf("Novelty:             %.2f%%\n", nov * 100);
        System.out.printf("Diversity:           %.1f origins\n", div);
        System.out.printf("Serendipity:         %.2f%%\n", seren * 100);
        System.out.printf("Coverage:            %.2f%%\n", cov);
        System.out.println("-------------------------------------------\n");
    }

}
