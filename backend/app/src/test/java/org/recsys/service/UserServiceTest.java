package org.recsys.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.recsys.dto.user.UserLoginRequest;
import org.recsys.dto.user.UserSignupRequest;
import org.recsys.model.User;
import org.recsys.repository.UserRepository;
import org.recsys.testutil.TestDataFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserPreferencesService preferencesService;

    @InjectMocks
    UserService userService;

    @Test
    void signup_shouldSaveUser_whenValidRequest() {
        // given
        UserSignupRequest req = TestDataFactory.validSignup();
        when(userRepository.findByEmail(req.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(req.getPassword())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        // when
        User res = userService.signup(req);
        // then
        assertNotNull(res);
        assertEquals(req.getName(), res.getName());
        assertEquals(req.getEmail(), res.getEmail());
        verify(userRepository).findByEmail(req.getEmail());
        verify(passwordEncoder).encode(req.getPassword());
        verify(userRepository).save(any(User.class));
        verify(preferencesService).setDefaultPreferencesForUser(res);
    }

    @Test
    void signup_shouldThrowException_whenUserAlreadyExists() {
        // given
        UserSignupRequest req = TestDataFactory.validSignup();
        // when
        when(userRepository.findByEmail(req.getEmail())).thenReturn(Optional.of(new User()));
        assertThrows(RuntimeException.class, () -> userService.signup(req));
        // then
        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void login_shouldReturnUser_whenCorrectCredentials() {
        // given
        String hashedPassword = "hashedPW";
        UserLoginRequest req = TestDataFactory.validLogin();
        User user = TestDataFactory.createUser(req.getEmail(), hashedPassword);
        // when
        when(userRepository.findByEmail(req.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(req.getPassword(), hashedPassword)).thenReturn(true);
        User res = userService.login(req);
        // then
        assertNotNull(res);
        assertEquals(user.getId(), res.getId());
        assertEquals(user.getEmail(), res.getEmail());
        verify(userRepository).findByEmail(req.getEmail());
    }

    @Test
    void login_shouldThrowException_whenPasswordIsWrong() {
        // given
        String hashedPassword = "hashedPW";
        UserLoginRequest req = TestDataFactory.invalidLogin();
        User user = TestDataFactory.createUser(req.getEmail(), hashedPassword);
        // when
        when(userRepository.findByEmail(req.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(req.getPassword(), hashedPassword)).thenReturn(false);
        // then
        assertThrows(BadCredentialsException.class, () -> userService.login(req));
    }

    @Test
    void loadUserByUsername_WhenUserExists_ShouldReturnUserDetails() {
        // given
        User mockUser = TestDataFactory.createUser();
        when(userRepository.findByEmail(mockUser.getEmail())).thenReturn(Optional.of(mockUser));
        // when
        UserDetails userDetails = userService.loadUserByUsername(mockUser.getEmail());
        // then
        assertNotNull(userDetails);
        assertEquals(mockUser.getEmail(), userDetails.getUsername());
        assertEquals(mockUser.getPasswordHash(), userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
        verify(userRepository, times(1)).findByEmail(mockUser.getEmail());
    }

    @Test
    void loadUserByUsername_WhenUserDoesNotExist_ShouldThrowException() {
        // given
        String nonExistentUsersEmail = "nosuchuser@gmail.com";
        when(userRepository.findByEmail(nonExistentUsersEmail)).thenReturn(Optional.empty());
        // when, then
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userService.loadUserByUsername(nonExistentUsersEmail));
        assertTrue(exception.getMessage().contains("Invalid email or password"));
        verify(userRepository, times(1)).findByEmail(nonExistentUsersEmail);
    }
}
