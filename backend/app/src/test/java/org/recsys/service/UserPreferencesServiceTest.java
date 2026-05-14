package org.recsys.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.recsys.dto.coffee.CoffeeVectorizationDto;
import org.recsys.dto.user.OnboardingRequest;
import org.recsys.model.ExperienceLevel;
import org.recsys.model.PrepMethod;
import org.recsys.model.RoastLevel;
import org.recsys.model.User;
import org.recsys.model.UserPreferences;
import org.recsys.repository.UserPreferencesRepository;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class UserPreferencesServiceTest {

    @Mock
    private UserPreferencesRepository preferencesRepository;

    @Mock
    private CoffeeVectorService vectorService;

    @Mock
    private WeightVectorService weightVectorService;

    @InjectMocks
    private UserPreferencesService userPreferencesService;

    private final Long userId = 1L;
    private UserPreferences existingPrefs;

    @BeforeEach
    void setUp() {
        existingPrefs = UserPreferences.builder()
                .userId(userId)
                .experienceLevel(ExperienceLevel.BEGINNER)
                .prepMethod(PrepMethod.IMMERSION)
                .tasteProfile(new float[] { 0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1.0f })
                .build();
    }

    @Test
    void setDefaultPreferencesForUser_ShouldReturnSavedPreferences() {
        // given
        User user = new User();
        user.setId(userId);
        when(preferencesRepository.save(any(UserPreferences.class))).thenAnswer(i -> i.getArguments()[0]);
        // when
        UserPreferences result = userPreferencesService.setDefaultPreferencesForUser(user);
        // then
        assertEquals(user, result.getUser());
        assertEquals(ExperienceLevel.BEGINNER, result.getExperienceLevel());
        assertNull(result.getTasteProfile());
        verify(preferencesRepository).save(any(UserPreferences.class));
    }

    @Test
    void getUserPreferencesByUserId_WhenFound_ShouldReturnOptionalWithData() {
        // given
        Long userId = 1L;
        UserPreferences prefs = UserPreferences.builder().userId(userId).build();
        when(preferencesRepository.findById(userId)).thenReturn(Optional.of(prefs));
        // when
        Optional<UserPreferences> result = userPreferencesService.getUserPreferencesByUserId(userId);
        // then
        assertTrue(result.isPresent());
        assertEquals(userId, result.get().getUserId());
        verify(preferencesRepository).findById(userId);
    }

    @Test
    void getUserPreferencesByUserId_WhenNotFound_ShouldReturnEmptyOptional() {
        // given
        Long userId = 2L;
        when(preferencesRepository.findById(userId)).thenReturn(Optional.empty());
        // when
        Optional<UserPreferences> result = userPreferencesService.getUserPreferencesByUserId(userId);
        // then
        assertFalse(result.isPresent());
        verify(preferencesRepository).findById(userId);
    }

    @Test
    void getUserPreferenceFlavorProfile_WhenPrefsExist_ShouldReturnProfile() {
        // given
        when(preferencesRepository.findById(userId)).thenReturn(Optional.of(existingPrefs));
        // when
        float[] result = userPreferencesService.getUserPreferenceFlavorProfile(userId);
        // then
        assertArrayEquals(existingPrefs.getTasteProfile(), result);
    }

    @Test
    void getUserPreferenceFlavorProfile_WhenPrefsMissing_ShouldReturnFallback() {
        // given
        float[] fallback = new float[] { 0f, 0f };
        when(preferencesRepository.findById(userId)).thenReturn(Optional.empty());
        when(vectorService.createBaseVector()).thenReturn(fallback);
        // when
        float[] result = userPreferencesService.getUserPreferenceFlavorProfile(userId);
        // then
        assertArrayEquals(fallback, result);
    }

    @Test
    void updateUserPreferencesAfterOnboarding_ShouldUpdateAndSave() {
        // given
        OnboardingRequest request = new OnboardingRequest(userId, ExperienceLevel.EXPERT, PrepMethod.ESPRESSO);
        float[] mockVector = new float[10];

        when(preferencesRepository.findById(userId)).thenReturn(Optional.of(existingPrefs));
        when(vectorService.createFlavorVector(any(CoffeeVectorizationDto.class))).thenReturn(mockVector);
        // when
        userPreferencesService.updateUserPreferencesAfterOnboarding(request);
        // then
        assertEquals(ExperienceLevel.EXPERT, existingPrefs.getExperienceLevel());
        assertEquals(PrepMethod.ESPRESSO, existingPrefs.getPrepMethod());
        assertArrayEquals(mockVector, existingPrefs.getTasteProfile());
        verify(preferencesRepository).save(existingPrefs);
    }

    @Test
    void updatePrepMethod_WhenValuesAreSame_ShouldDoNothing() {
        // given
        OnboardingRequest request = new OnboardingRequest(userId, ExperienceLevel.BEGINNER, PrepMethod.IMMERSION);
        when(preferencesRepository.findById(userId)).thenReturn(Optional.of(existingPrefs));
        // when
        userPreferencesService.updatePrepMethod(request);
        // then
        verify(vectorService, never()).createFlavorVector(any());
        verify(preferencesRepository, never()).save(any());
    }

    @Test
    void updatePrepMethod_WhenValuesChanged_ShouldCalculateWeightedProfile() {
        // given , change from IMMERSION to COLD_BREW
        OnboardingRequest request = new OnboardingRequest(userId, ExperienceLevel.BEGINNER, PrepMethod.COLD_BREW);

        float[] oldBase = new float[10];
        float[] newBase = new float[10];
        float[] normalizedVector = new float[10]; // mocked result
        when(preferencesRepository.findById(userId)).thenReturn(Optional.of(existingPrefs));
        when(vectorService.createFlavorVector(any(CoffeeVectorizationDto.class))).thenReturn(oldBase, newBase);
        when(weightVectorService.l2Normalize(any(float[].class))).thenReturn(normalizedVector);
        // when
        userPreferencesService.updatePrepMethod(request);
        // then
        assertEquals(PrepMethod.COLD_BREW, existingPrefs.getPrepMethod());
        assertArrayEquals(normalizedVector, existingPrefs.getTasteProfile());
        verify(weightVectorService).l2Normalize(any(float[].class));
        verify(preferencesRepository).save(existingPrefs);
    }

    @Test
    void calculateNewUserVector_ShouldApplySpecificRulesForEspressoAndAdvanced() {
        // given
        OnboardingRequest request = new OnboardingRequest(userId, ExperienceLevel.ADVANCED, PrepMethod.ESPRESSO);
        float[] mockReturn = new float[10];
        when(vectorService.createFlavorVector(any(CoffeeVectorizationDto.class))).thenReturn(mockReturn);
        // when
        userPreferencesService.calculateNewUserVector(request, false);
        // then
        var dtoCaptor = org.mockito.ArgumentCaptor.forClass(CoffeeVectorizationDto.class);
        verify(vectorService).createFlavorVector(dtoCaptor.capture());
        CoffeeVectorizationDto dto = dtoCaptor.getValue();
        // Validate logic: Base Bitter (5) + Espresso (+2) = 7
        assertEquals(7, dto.getBitterness());
        // Validate Advanced Experience logic
        assertEquals(83.5, dto.getScaScore());
    }

    @Test
    void calculateNewUserVector_ShouldApplySpecificRulesForPouroverAndIntermediate() {
        // given
        OnboardingRequest request = new OnboardingRequest(userId, ExperienceLevel.INTERMEDIATE, PrepMethod.POUROVER);
        float[] mockReturn = new float[10];
        when(vectorService.createFlavorVector(any(CoffeeVectorizationDto.class))).thenReturn(mockReturn);
        // when
        userPreferencesService.calculateNewUserVector(request, true);
        // then
        var dtoCaptor = org.mockito.ArgumentCaptor.forClass(CoffeeVectorizationDto.class);
        verify(vectorService).createFlavorVector(dtoCaptor.capture());
        CoffeeVectorizationDto dto = dtoCaptor.getValue();
        /// Acidity: Base(5) + Pourover(+2) = 7
        assertEquals(7, dto.getAcidity());
        // Body: Base(5) - Pourover(2) = 3
        assertEquals(3, dto.getBody());
        // Sweetness: Base(6) + Intermediate(+1) = 7
        assertEquals(7, dto.getSweetness());
        // Aftertaste: Base(5) + Intermediate(+1) = 6
        assertEquals(6, dto.getAftertaste());
        // Roast: MEDIUM_LIGHT ordinal is 1
        assertEquals(RoastLevel.MEDIUM_LIGHT.ordinal(), dto.getRoastLevel());
        // Altitude: Intermediate falls into the 'default' range (1000, 1900)
        assertEquals(1000, dto.getAltitude().lower());
        assertEquals(1900, dto.getAltitude().upper());
    }

    @Test
    void updatePrepMethod_ShouldThrowException_WhenUserNotFound() {
        // given
        OnboardingRequest request = new OnboardingRequest(99L, ExperienceLevel.BEGINNER, PrepMethod.ESPRESSO);
        when(preferencesRepository.findById(99L)).thenReturn(Optional.empty());
        // when & then
        assertThrows(EntityNotFoundException.class, () -> userPreferencesService.updatePrepMethod(request));
    }
}
