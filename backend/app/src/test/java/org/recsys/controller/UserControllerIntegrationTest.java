package org.recsys.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.recsys.config.TestConfig;
import org.recsys.dto.user.InteractionRequest;
import org.recsys.dto.user.OnboardingRequest;
import org.recsys.dto.user.UserLoginRequest;
import org.recsys.dto.user.UserSignupRequest;
import org.recsys.model.ExperienceLevel;
import org.recsys.model.PrepMethod;
import org.recsys.model.User;
import org.recsys.model.UserPreferences;
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

import com.jayway.jsonpath.JsonPath;

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
        User saved = userRepository.save(user);

        UserPreferences prefs = new UserPreferences();
        prefs.setUser(user);
        prefs.setExperienceLevel(ExperienceLevel.BEGINNER);
        prefs.setPrepMethod(PrepMethod.OTHER);
        float[] mockProfile = new float[30];
        java.util.Arrays.fill(mockProfile, 0.5f);
        prefs.setTasteProfile(mockProfile);
        preferencesRepository.save(prefs);

        return saved;
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

    @Test
    void deleteUser_shouldReturnNoContent_whenUserExists() throws Exception {
        // given
        User user = prepareUser();
        String token = getJwtToken();
        // when & then
        mockMvc.perform(delete(USER_API + "/" + user.getId().toString())
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        assertFalse(userRepository.existsById(user.getId()));
    }

    @Test
    void shouldUpdatePreferencesAfterSurvey() throws Exception {
        // given
        User user = prepareUser();
        String token = getJwtToken();
        OnboardingRequest request = new OnboardingRequest(user.getId(), ExperienceLevel.ADVANCED, PrepMethod.POUROVER);
        // when
        mockMvc.perform(post(USER_API + "/preferences")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        // then
        mockMvc.perform(get(USER_API + "/{userId}/preferences", user.getId())
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.experienceLevel").value("ADVANCED"))
                .andExpect(jsonPath("$.prepMethod").value("POUROVER"));
    }

    @Test
    void shouldUpdatePrepMethodAndAdjustVector() throws Exception {
        // given
        User user = prepareUser();
        String token = getJwtToken();
        OnboardingRequest request = new OnboardingRequest(user.getId(), null, PrepMethod.COLD_BREW);
        // when & then
        mockMvc.perform(post(USER_API + "/preferences/preparation")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void getUserPreferences_ShouldReturnNotFound_WhenUserMissing() throws Exception {
        // given
        prepareUser();
        String token = getJwtToken();
        // when & then
        mockMvc.perform(get(USER_API + "/99999404/preferences")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void addUserInteraction_Success_ShouldReturnOk() throws Exception {
        // given
        User user = prepareUser();
        String token = getJwtToken();
        InteractionRequest request = new InteractionRequest(user.getId(), 1L, true, 4);
        // when & then
        mockMvc.perform(post(USER_API + "/interactions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    private String getJwtToken() throws Exception {
        UserLoginRequest loginReq = TestDataFactory.validLogin();

        String response = mockMvc.perform(post(USER_API + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return JsonPath.read(response, "$.token");
    }
}
