package org.recsys.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.recsys.model.ExperienceLevel;
import org.recsys.model.User;
import org.recsys.model.UserPreferences;
import org.recsys.repository.UserPreferencesRepository;

@ExtendWith(MockitoExtension.class)
class UserPreferencesServiceTest {

    @Mock
    private UserPreferencesRepository preferencesRepository;

    @Mock
    private CoffeeVectorService vectorService;

    @InjectMocks
    private UserPreferencesService userPreferencesService;

    @Test
    void setDefaultPreferencesForUser_ShouldReturnSavedPreferences() {
        // given
        User user = new User();
        user.setId(1L);

        when(preferencesRepository.save(any(UserPreferences.class))).thenAnswer(i -> i.getArguments()[0]);
        // when
        UserPreferences result = userPreferencesService.setDefaultPreferencesForUser(user);
        // then
        assertEquals(user, result.getUser());
        assertEquals(ExperienceLevel.BEGINNER, result.getExperienceLevel());
        assertNull(result.getTasteProfile());
        assertNull(result.getPrepMethod());

        verify(preferencesRepository, times(1)).save(any(UserPreferences.class));
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
}
