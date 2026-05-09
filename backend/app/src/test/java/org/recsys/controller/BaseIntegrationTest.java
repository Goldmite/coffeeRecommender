package org.recsys.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.recsys.config.TestConfig;
import org.recsys.dto.user.UserLoginRequest;
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

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(TestConfig.class)
@Transactional
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected UserPreferencesRepository preferencesRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    private EntityManager entityManager;

    protected String token;
    protected User testUser;

    @BeforeEach
    void setUp() throws Exception {
        // clean db
        preferencesRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        // setup Data
        testUser = prepareUser();
        // flush
        userRepository.flush();
        preferencesRepository.flush();
        // authenticate
        token = "Bearer " + fetchJwtToken();
    }

    private User prepareUser() {
        User user = new User();
        user.setName(TestDataFactory.testName);
        user.setEmail(TestDataFactory.testEmail);
        user.setPasswordHash(passwordEncoder.encode(TestDataFactory.testPassword));
        User saved = userRepository.saveAndFlush(user);

        UserPreferences prefs = new UserPreferences();
        prefs.setUser(saved);
        // prefs.setUserId(saved.getId());
        prefs.setExperienceLevel(ExperienceLevel.BEGINNER);
        prefs.setPrepMethod(PrepMethod.OTHER);

        float[] mockProfile = new float[30];
        java.util.Arrays.fill(mockProfile, 0.5f);
        prefs.setTasteProfile(mockProfile);

        preferencesRepository.saveAndFlush(prefs);

        entityManager.clear();

        return saved;
    }

    private String fetchJwtToken() throws Exception {
        UserLoginRequest loginReq = new UserLoginRequest(
                TestDataFactory.testEmail,
                TestDataFactory.testPassword);

        String response = mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return JsonPath.read(response, "$.token");
    }
}