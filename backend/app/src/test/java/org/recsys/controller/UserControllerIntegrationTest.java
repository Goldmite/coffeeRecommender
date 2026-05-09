package org.recsys.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import org.junit.jupiter.api.Test;
import org.recsys.dto.user.InteractionRequest;
import org.recsys.dto.user.OnboardingRequest;
import org.recsys.dto.user.UserLoginRequest;
import org.recsys.dto.user.UserSignupRequest;
import org.recsys.model.ExperienceLevel;
import org.recsys.model.PrepMethod;
import org.recsys.repository.UserRepository;
import org.recsys.testutil.TestDataFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest extends BaseIntegrationTest {

    private static String USER_API = "/api/users";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void signup_shouldBeSuccessful_whenRequestIsValid() throws Exception {
        // given
        preferencesRepository.deleteAll();
        userRepository.deleteAll();
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
        preferencesRepository.deleteAll();
        userRepository.deleteAll();
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
        Long userId = testUser.getId();
        // when & then
        mockMvc.perform(delete(USER_API + "/" + userId.toString())
                .header("Authorization", token))
                .andExpect(status().isNoContent());

        assertFalse(userRepository.existsById(userId));
    }

    @Test
    void shouldUpdatePreferencesAfterSurvey() throws Exception {
        // given
        OnboardingRequest request = new OnboardingRequest(testUser.getId(), ExperienceLevel.ADVANCED,
                PrepMethod.POUROVER);
        // when
        mockMvc.perform(post(USER_API + "/preferences")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        // then
        mockMvc.perform(get(USER_API + "/{userId}/preferences", testUser.getId())
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.experienceLevel").value("ADVANCED"))
                .andExpect(jsonPath("$.prepMethod").value("POUROVER"));
    }

    @Test
    void shouldUpdatePrepMethodAndAdjustVector() throws Exception {
        // given
        OnboardingRequest request = new OnboardingRequest(testUser.getId(), null, PrepMethod.COLD_BREW);
        // when & then
        mockMvc.perform(post(USER_API + "/preferences/preparation")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void getUserPreferences_ShouldReturnNotFound_WhenUserMissing() throws Exception {
        // when & then
        mockMvc.perform(get(USER_API + "/99999404/preferences")
                .header("Authorization", token))
                .andExpect(status().isNotFound());
    }

    @Test
    void addUserInteraction_Success_ShouldReturnOk() throws Exception {
        // given
        InteractionRequest request = new InteractionRequest(testUser.getId(), 1L, true, 4);
        // when & then
        mockMvc.perform(post(USER_API + "/interactions")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
