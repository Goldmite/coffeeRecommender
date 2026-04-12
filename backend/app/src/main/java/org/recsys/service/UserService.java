package org.recsys.service;

import org.recsys.dto.user.UserLoginRequest;
import org.recsys.dto.user.UserResponse;
import org.recsys.dto.user.UserSignupRequest;
import org.recsys.exception.InvalidCredentialsException;
import org.recsys.model.User;
import org.recsys.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityExistsException;
import jakarta.transaction.Transactional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    private static final String INVALID_EMAIL_OR_PASSWORD = "Invalid email or password";
    private static final String USER_NOT_FOUND = "User not found: ";
    private static final String USER_ALREADY_EXISTS = "User already exists";

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND + email));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .authorities("USER")
                .build();
    }

    @Transactional
    public User signup(UserSignupRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EntityExistsException(USER_ALREADY_EXISTS);
        }

        User newUser = new User();
        newUser.setName(request.getName());
        newUser.setEmail(request.getEmail());
        newUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        return userRepository.save(newUser);
    }

    public User login(UserLoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException(INVALID_EMAIL_OR_PASSWORD));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException(INVALID_EMAIL_OR_PASSWORD);
        }

        return user;
    }

    /*
     * Add when needed and test it
     * public User getUserById(Long id) {
     * return userRepository.findById(id)
     * .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));
     * }
     */
}
