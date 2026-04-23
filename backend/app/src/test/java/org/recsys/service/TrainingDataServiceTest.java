package org.recsys.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.recsys.dto.recommendation.PreparedTrainingData;
import org.recsys.dto.recommendation.RatingTriplet;
import org.recsys.model.UserInteractions;
import org.recsys.repository.UserInteractionsRepository;

@ExtendWith(MockitoExtension.class)
class TrainingDataServiceTest {

    @Mock
    private UserInteractionsRepository interactionRepository;

    @InjectMocks
    private TrainingDataService trainingDataService;

    @Test
    void prepareData_ShouldCorrectlyMapIndicesAndCalculateScores() {
        // given
        long userId = 101L;
        UserInteractions interaction1 = UserInteractions.builder()
                .userId(userId)
                .coffeeId(500L)
                .rating(4)
                .createdAt(Instant.now())
                .build();

        UserInteractions interaction2 = UserInteractions.builder()
                .userId(userId)
                .coffeeId(600L)
                .rating(null)
                .isPurchased(true)
                .createdAt(Instant.now())
                .build();

        when(interactionRepository.findAll()).thenReturn(Arrays.asList(interaction1, interaction2));
        // when
        PreparedTrainingData result = trainingDataService.prepareData(false);
        // then
        assertNotNull(result);
        List<RatingTriplet> triplets = result.triplets();
        assertEquals(2, triplets.size());
        // verify Index Mapping: should have the same internal index for both
        assertEquals(triplets.get(0).userIndex(), triplets.get(1).userIndex());
        assertEquals(4.0f, triplets.get(0).score());
        assertEquals(5.0f, triplets.get(1).score());
        // verify global mean: (4.0 + 5.0) / 2 = 4.5
        assertEquals(4.5f, result.globalMean(), 0.001f);
    }

    @Test
    void prepareData_ShouldHandleEmptyInteractions() {
        // given
        when(interactionRepository.findAll()).thenReturn(List.of());
        // when
        PreparedTrainingData result = trainingDataService.prepareData(true);
        // then
        assertTrue(result.triplets().isEmpty());
        assertEquals(0.0f, result.globalMean());
    }

    @Test
    void shouldCalculateScoreByClickWhenInteractionOnlyHasClicked() {
        // given
        UserInteractions clickOnly = UserInteractions.builder()
                .userId(1L)
                .coffeeId(1L)
                .isClicked(true)
                .isPurchased(false)
                .rating(null)
                .createdAt(Instant.now())
                .build();
        when(interactionRepository.findAll()).thenReturn(List.of(clickOnly));
        // when
        PreparedTrainingData result = trainingDataService.prepareData(false);
        // then
        assertEquals(2.0f, result.triplets().getFirst().score(), "Click should result in score of 2.0");
    }

    @Test
    void shouldCalculateScoreByRatingAsItHasHighestPriority() {
        // given
        UserInteractions rated = UserInteractions.builder()
                .userId(1L)
                .coffeeId(1L)
                .isClicked(true)
                .isPurchased(true)
                .rating(3)
                .createdAt(Instant.now())
                .build();
        when(interactionRepository.findAll()).thenReturn(List.of(rated));
        // when
        PreparedTrainingData result = trainingDataService.prepareData(false);
        // then
        assertEquals(3.0f, result.triplets().getFirst().score());
    }

    @Test
    void shouldReturnZeroScoreWhenNoInteractions() {
        // given
        UserInteractions rated = UserInteractions.builder()
                .userId(1L)
                .coffeeId(1L)
                .isClicked(false)
                .isPurchased(false)
                .rating(null)
                .createdAt(Instant.now())
                .build();
        when(interactionRepository.findAll()).thenReturn(List.of(rated));
        // when
        PreparedTrainingData result = trainingDataService.prepareData(false);
        // then
        assertEquals(0.0f, result.triplets().getFirst().score());
    }
}
