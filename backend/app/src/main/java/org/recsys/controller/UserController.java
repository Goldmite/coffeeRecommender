package org.recsys.controller;

import org.recsys.config.jwt.JwtUtils;
import org.recsys.dto.user.AuthResponse;
import org.recsys.dto.user.InteractionRequest;
import org.recsys.dto.user.OnboardingRequest;
import org.recsys.dto.user.UserLoginRequest;
import org.recsys.dto.user.UserResponse;
import org.recsys.dto.user.UserSignupRequest;
import org.recsys.service.UserInteractionsService;
import org.recsys.service.UserPreferencesService;
import org.recsys.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final AuthenticationManager authManager;
    private final JwtUtils jwtUtils;
    private final UserService userService;
    private final UserPreferencesService prefService;
    private final UserInteractionsService interactionsService;

    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signup(@Valid @RequestBody UserSignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.fromEntity(userService.signup(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody UserLoginRequest request) {
        authManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        UserResponse userRes = UserResponse.fromEntity(userService.login(request));
        boolean isNew = prefService.isUserUsingDefaultPreferences(userRes.getId());
        String token = jwtUtils.generateToken(request.getEmail(), userRes.getId(), isNew);

        return ResponseEntity.ok(new AuthResponse(token, userRes));
    }

    @PostMapping("/preferences")
    public ResponseEntity<?> updatePreferencesAfterSurvey(@RequestBody OnboardingRequest request) {
        prefService.updateUserPreferencesAfterOnboarding(request);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/interactions")
    public ResponseEntity<?> addUserInteraction(@Valid @RequestBody InteractionRequest request) {
        interactionsService.addInteraction(request);

        return ResponseEntity.ok().build();
    }
}
