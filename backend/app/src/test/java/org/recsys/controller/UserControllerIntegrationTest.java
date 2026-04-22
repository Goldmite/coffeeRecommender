package org.recsys.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.recsys.config.TestConfig;
import org.recsys.dto.user.UserLoginRequest;
import org.recsys.dto.user.UserSignupRequest;
import org.recsys.model.User;
import org.recsys.repository.UserPreferencesRepository;
import org.recsys.repository.UserRepository;
import org.recsys.testutil.TestDataFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(TestConfig.class)
class UserControllerIntegrationTest {

    private static String USER_API = "/api/users";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserPreferencesRepository preferencesRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        preferencesRepository.deleteAll();
        userRepository.deleteAll();
    }

    private User prepareUser() {
        User user = new User();
        user.setName(TestDataFactory.testName);
        user.setEmail(TestDataFactory.testEmail);
        user.setPasswordHash(passwordEncoder.encode(TestDataFactory.testPassword));
        return userRepository.save(user);
    }

    @Test
    void signup_shouldBeSuccessful_whenRequestIsValid() throws Exception {
        // given
        UserSignupRequest req = TestDataFactory.validSignup();
        // when & then
        mockMvc.perform(post(USER_API + "/signup")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    void signup_shouldReturnBadRequest_whenRequestIsInvalid() throws Exception {
        // given
        UserSignupRequest invalidReq = TestDataFactory.invalidSignup();
        // when & then
        mockMvc.perform(post(USER_API + "/signup")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.email").exists());
    }

    @Test
    void login_shouldBeSuccessful() throws Exception {
        // given
        UserLoginRequest req = TestDataFactory.validLogin();
        prepareUser();
        // when & then
        mockMvc.perform(post(USER_API + "/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value(req.getEmail()));
    }

    @Test
    void login_shouldReturnUnauthorized_whenWrongPassword() throws Exception {
        // given
        UserLoginRequest req = TestDataFactory.validLogin();
        req.setPassword("wrong password");
        prepareUser();
        // when & then
        mockMvc.perform(post(USER_API + "/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_shouldReturnBadRequest_whenRequestIsInvalid() throws Exception {
        // given
        UserLoginRequest invalidReq = TestDataFactory.invalidLogin();
        prepareUser();
        // when & then
        mockMvc.perform(post(USER_API + "/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").exists());
    }
}
