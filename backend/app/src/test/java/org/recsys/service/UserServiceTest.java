package org.recsys.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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
import org.recsys.dto.user.UserResponse;
import org.recsys.dto.user.UserSignupRequest;
import org.recsys.model.User;
import org.recsys.repository.UserRepository;
import org.recsys.testutil.TestDataFactory;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    UserService userService;

    @Test
    void signup_shouldSaveUser_whenValidRequest() {
        // given
        UserSignupRequest req = TestDataFactory.validSignup();
        // when
        when(userRepository.findByEmail(req.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(req.getPassword())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        UserResponse res = userService.signup(req);
        // then
        assertNotNull(res);
        assertEquals(req.getName(), res.getName());
        assertEquals(req.getEmail(), res.getEmail());
        verify(userRepository).findByEmail(req.getEmail());
        verify(passwordEncoder).encode(req.getPassword());
        verify(userRepository).save(any(User.class));
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
        UserResponse res = userService.login(req);
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
        assertThrows(RuntimeException.class, () -> userService.login(req));
    }

}
