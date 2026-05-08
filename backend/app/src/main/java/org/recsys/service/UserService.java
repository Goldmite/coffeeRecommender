package org.recsys.service;

import org.recsys.dto.user.UserLoginRequest;
import org.recsys.dto.user.UserSignupRequest;
import org.recsys.model.User;
import org.recsys.repository.UserInteractionsRepository;
import org.recsys.repository.UserPreferencesRepository;
import org.recsys.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserInteractionsRepository interactionsRepository;
    private final UserPreferencesService preferencesService;
    private final UserPreferencesRepository preferencesRepository;

    private static final String INVALID_EMAIL_OR_PASSWORD = "Invalid email or password";
    private static final String USER_ALREADY_EXISTS = "User already exists";

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(INVALID_EMAIL_OR_PASSWORD));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .authorities("ROLE_" + user.getRole().name())
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

        User user = userRepository.save(newUser);

        preferencesService.setDefaultPreferencesForUser(user);

        return user;
    }

    public User login(UserLoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException(INVALID_EMAIL_OR_PASSWORD));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException(INVALID_EMAIL_OR_PASSWORD);
        }

        return user;
    }

    @Transactional
    public void deleteUser(Long id) {
        if (userRepository.findById(id).isEmpty()) {
            throw new EntityNotFoundException();
        }
        preferencesRepository.deleteByUserId(id);
        // delete interactions
        // TODO: can keep, but they will decay over time
        interactionsRepository.deleteByUserId(id);

        userRepository.deleteById(id);
        log.info("User " + id + " account and data successfully erased.");
    }
}
