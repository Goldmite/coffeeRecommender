package org.recsys.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.recsys.dto.coffee.CoffeeBeanRequest;
import org.recsys.model.RoastLevel;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;

@SpringBootTest
@AutoConfigureMockMvc
class CoffeeControllerIntegrationTest extends BaseIntegrationTest {

    private static final String COFFEE_API = "/api/coffee";

    @Test
    void getCoffeeById_ShouldReturnCoffee_WhenIdExists() throws Exception {
        // given
        Long coffeeId = 1L;
        // when & then
        mockMvc.perform(get(COFFEE_API)
                .header("Authorization", token)
                .param("id", coffeeId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(coffeeId))
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.shop.name").exists());
    }

    @Test
    void getPurchasedCoffeesByUser_ShouldReturnPagedResults() throws Exception {
        // given
        Long userId = testUser.getId();
        Integer page = 0;
        Integer size = 5;
        // when & then
        mockMvc.perform(get(COFFEE_API + "/purchased/user/" + userId)
                .header("Authorization", token)
                .param("page", page.toString())
                .param("size", size.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page.size").value(size))
                .andExpect(jsonPath("$.page.number").value(page));
    }

    @Test
    void addNewCoffee_ShouldReturnCreated() throws Exception {
        // given
        CoffeeBeanRequest.FeaturesRequest features = CoffeeBeanRequest.FeaturesRequest.builder()
                .origins(List.of("Ethiopia"))
                .roastLevel(RoastLevel.LIGHT)
                .scaScore(88.0)
                .acidity(4)
                .body(3)
                .build();

        CoffeeBeanRequest request = CoffeeBeanRequest.builder()
                .name("Limited Batch #1")
                .price(new BigDecimal("24.50"))
                .productUrl("https://shop.com/coffee1")
                .shopId(1)
                .features(features)
                .build();
        // when & then
        mockMvc.perform(post(COFFEE_API)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Limited Batch #1"))
                .andExpect(jsonPath("$.scaScore").value(88.0));
    }

    @Test
    void deleteCoffeeById_ShouldReturnNoContent() throws Exception {
        // given
        Long idToDelete = 10L;
        // when & then
        mockMvc.perform(delete(COFFEE_API)
                .header("Authorization", token)
                .param("id", idToDelete.toString()))
                .andExpect(status().isNoContent());
    }

    @Test
    void batchUpdateVectors_ShouldReturnUpdatedList() throws Exception {
        // when & then
        mockMvc.perform(post(COFFEE_API + "/vector")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
