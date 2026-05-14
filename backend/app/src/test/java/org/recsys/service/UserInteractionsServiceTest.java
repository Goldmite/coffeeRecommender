package org.recsys.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.recsys.dto.user.InteractionRequest;
import org.recsys.model.CoffeeBean;
import org.recsys.model.CoffeeFeatures;
import org.recsys.model.User;
import org.recsys.model.UserInteractions;
import org.recsys.model.UserPreferences;
import org.recsys.repository.CoffeeRepository;
import org.recsys.repository.UserInteractionsRepository;
import org.recsys.repository.UserPreferencesRepository;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class UserInteractionsServiceTest {

    @Mock
    private FlavorVectorService flavorVectorService;

    @Mock
    private UserInteractionsRepository interactionsRepository;

    @Mock
    private CoffeeRepository coffeeRepository;

    @Mock
    private UserPreferencesRepository preferencesRepository;

    @InjectMocks
    private UserInteractionsService userInteractionsService;

    private UserPreferences userPref;
    private CoffeeBean coffee;
    private float[] initialProfile;
    private float[] coffeeVector;

    @BeforeEach
    void setUp() {
        initialProfile = new float[] { 0.1f, 0.1f };
        coffeeVector = new float[] { 0.5f, 0.5f };

        User user = new User();
        user.setId(1L);

        userPref = UserPreferences.builder()
                .user(user)
                .tasteProfile(initialProfile)
                .build();

        coffee = new CoffeeBean();
        coffee.setId(100L);
        CoffeeFeatures features = new CoffeeFeatures();
        features.setFlavorVector(coffeeVector);
        coffee.setFeatures(features);
    }

    @Test
    void addInteraction_WhenPurchased_ShouldApplyCorrectShiftStrength() {
        // given
        InteractionRequest request = new InteractionRequest(1L, 100L, true, null);
        float[] shiftedProfile = new float[] { 0.2f, 0.2f };
        when(preferencesRepository.findById(1L)).thenReturn(Optional.of(userPref));
        when(coffeeRepository.findById(100L)).thenReturn(Optional.of(coffee));
        when(interactionsRepository.findById(any())).thenReturn(Optional.empty());
        when(interactionsRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(flavorVectorService.calculateVectorShift(initialProfile, coffeeVector, 0.015f))
                .thenReturn(shiftedProfile);
        // when
        UserInteractions result = userInteractionsService.addInteraction(request);
        // then
        assertTrue(result.getIsPurchased());
        assertNotNull(result.getPurchaseDate());
        assertArrayEquals(shiftedProfile, userPref.getTasteProfile());
        verify(preferencesRepository).save(userPref);
    }

    @Test
    void addInteraction_WhenRated_ShouldCalculateAlphaDifference() {
        // given: Existing interaction with rating 3 (oldAlpha = 0)
        // Request rating 5 (newAlpha = (5-3)*0.05 = 0.1)
        InteractionRequest request = new InteractionRequest(1L, 100L, false, 5);
        UserInteractions existingInteraction = new UserInteractions();
        existingInteraction.setRating(3);
        when(preferencesRepository.findById(1L)).thenReturn(Optional.of(userPref));
        when(coffeeRepository.findById(100L)).thenReturn(Optional.of(coffee));
        when(interactionsRepository.findById(any())).thenReturn(Optional.of(existingInteraction));
        when(interactionsRepository.save(any())).thenReturn(existingInteraction);
        // Expected shift: 0.1 (new) - 0.0 (old) = 0.1f
        when(flavorVectorService.calculateVectorShift(any(), any(), eq(0.1f)))
                .thenReturn(new float[] { 0.9f });
        // when
        userInteractionsService.addInteraction(request);
        // then
        assertEquals(5, existingInteraction.getRating());
        verify(flavorVectorService).calculateVectorShift(any(), any(), eq(0.1f));
    }

    @Test
    void addInteraction_ShouldThrowException_WhenUserNotFound() {
        // given
        InteractionRequest request = new InteractionRequest(99L, 100L, false, null);
        when(preferencesRepository.findById(99L)).thenReturn(Optional.empty());
        // when & then
        assertThrows(EntityNotFoundException.class, () -> userInteractionsService.addInteraction(request));
    }

    @Test
    void addInteraction_ShouldSetClickedToTrue_Always() {
        // given
        InteractionRequest request = new InteractionRequest(1L, 100L, false, null);
        when(preferencesRepository.findById(1L)).thenReturn(Optional.of(userPref));
        when(coffeeRepository.findById(100L)).thenReturn(Optional.of(coffee));
        when(interactionsRepository.findById(any())).thenReturn(Optional.empty());
        when(interactionsRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        // when
        UserInteractions result = userInteractionsService.addInteraction(request);
        // then
        assertTrue(result.getIsClicked());
    }
}
