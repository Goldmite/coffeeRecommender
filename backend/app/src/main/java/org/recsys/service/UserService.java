package org.recsys.service;

import org.recsys.dto.user.UserLoginRequest;
import org.recsys.dto.user.UserResponse;
import org.recsys.dto.user.UserSignupRequest;
import org.recsys.exception.InvalidCredentialsException;
import org.recsys.model.User;
import org.recsys.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    private static final String INVALID_EMAIL_OR_PASSWORD = "Invalid email or password";
    // private static final String USER_NOT_FOUND = "User not found";
    private static final String USER_ALREADY_EXISTS = "User already exists";

    public UserResponse signup(UserSignupRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException(USER_ALREADY_EXISTS);
        }

        User newUser = new User();
        newUser.setName(request.getName());
        newUser.setEmail(request.getEmail());
        newUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        User saved = userRepository.save(newUser);

        return new UserResponse(saved.getId(), saved.getName(), saved.getEmail());
    }

    public UserResponse login(UserLoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException(INVALID_EMAIL_OR_PASSWORD));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException(INVALID_EMAIL_OR_PASSWORD);
        }

        return new UserResponse(user.getId(), user.getName(), user.getEmail());
    }

    /*
     * Add when needed and test it
     * public User getUserById(Long id) {
     * return userRepository.findById(id)
     * .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));
     * }
     */
}
