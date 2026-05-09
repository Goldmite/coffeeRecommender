package org.recsys.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.recsys.dto.recommendation.FeatureFilterRequest;
import org.recsys.dto.recommendation.RecommendationFilterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;

import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class RecommendationControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    private final String BASE_URL = "/api/recommendation";

    @Test
    void getRecommendations_NoFilters() throws Exception {
        // given
        Long userId = testUser.getId();
        Integer limit = 10;
        RecommendationFilterRequest request = new RecommendationFilterRequest(null, null);
        // when
        String response = mockMvc.perform(post(BASE_URL)
                .header("Authorization", token)
                .param("userId", userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(limit))
                .andReturn()
                .getResponse().getContentAsString();
        // then
        assertSortedRecommendationList(response);
    }

    @Test
    void getRecommendations_OnlyShopFilters() throws Exception {
        // given
        Long userId = testUser.getId();
        Integer limit = 5;
        RecommendationFilterRequest request = new RecommendationFilterRequest(List.of(4, 5), null);
        // when
        String response = mockMvc.perform(post(BASE_URL)
                .header("Authorization", token)
                .param("userId", userId.toString())
                .param("limit", limit.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(limit))
                .andReturn()
                .getResponse().getContentAsString();
        ;
        // then
        assertSortedRecommendationList(response);
    }

    @Test
    void getRecommendations_OnlyFeatureFilters() throws Exception {
        // given
        Long userId = testUser.getId();
        int limit = 10;
        FeatureFilterRequest featureFilter = new FeatureFilterRequest(
                4.0f, 3.0f, null, null, null, null, null, null, null, null, null, null, null, null, null);
        RecommendationFilterRequest request = new RecommendationFilterRequest(null, featureFilter);
        // when
        String response = mockMvc.perform(post(BASE_URL)
                .header("Authorization", token)
                .param("userId", userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(limit))
                .andReturn()
                .getResponse().getContentAsString();
        ;
        // then
        assertSortedRecommendationList(response);
    }

    @Test
    void getRecommendations_BothFiltersProvided() throws Exception {
        // given
        Long userId = testUser.getId();
        int limit = 10;
        FeatureFilterRequest featureFilter = new FeatureFilterRequest(
                null, null, 5.0f, 5.0f, null, 1.0f, null, null, null, null, null, null, null, null, null);
        RecommendationFilterRequest request = new RecommendationFilterRequest(List.of(1, 2, 3), featureFilter);
        // when
        String response = mockMvc.perform(post(BASE_URL)
                .header("Authorization", token)
                .param("userId", userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(limit))
                .andReturn()
                .getResponse().getContentAsString();
        ;
        // then
        assertSortedRecommendationList(response);
    }

    private void assertSortedRecommendationList(String response) {
        List<Double> scores = JsonPath.read(response, "$[*].score");

        for (int i = 0; i < scores.size() - 1; i++) {
            assertTrue(scores.get(i) >= scores.get(i + 1),
                    "Scores should be in descending order. Found: " + scores.get(i) + " before " + scores.get(i + 1));
        }
    }
}
