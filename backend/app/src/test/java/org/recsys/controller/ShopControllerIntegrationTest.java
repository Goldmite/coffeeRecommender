package org.recsys.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.recsys.dto.shop.ShopRequest;
import org.recsys.dto.shop.ShopUpdateRequest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;

@SpringBootTest
@AutoConfigureMockMvc
class ShopControllerIntegrationTest extends BaseIntegrationTest {

    private static final String SHOPS_API = "/api/shops";

    @Test
    void getShops_ShouldReturnActiveShopsByDefault() throws Exception {
        // when & then
        mockMvc.perform(get(SHOPS_API)
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].is_active").value(true));
    }

    @Test
    void addShops_ShouldReturnCreated_WhenUserIsAdmin() throws Exception {
        // given
        ShopRequest newShop = new ShopRequest("Roast Master", "https://roastmaster.com", true);
        List<ShopRequest> request = List.of(newShop);
        // when & then
        mockMvc.perform(post(SHOPS_API)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Roast Master"))
                .andExpect(jsonPath("$[0].id").exists());
    }

    @Test
    void updateShopInfo_ShouldModifyExistingShop() throws Exception {
        // given
        ShopUpdateRequest update = new ShopUpdateRequest(1, "Updated Name", "https://new-url.com", false);
        List<ShopUpdateRequest> request = List.of(update);
        // when & then
        mockMvc.perform(put(SHOPS_API)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Updated Name"))
                .andExpect(jsonPath("$[0].is_active").value(false));
    }

    @Test
    void removeShops_ShouldReturnNoContent() throws Exception {
        // given
        List<Integer> idsToDelete = List.of(1, 2);
        // when & then
        mockMvc.perform(delete(SHOPS_API)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(idsToDelete)))
                .andExpect(status().isNoContent());
    }

    @Test
    void addShops_ShouldReturnBadRequest_WhenUrlIsInvalid() throws Exception {
        // given
        ShopRequest invalidShop = new ShopRequest("Bad Shop", "not-a-url", true);
        // when & then
        mockMvc.perform(post(SHOPS_API)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(invalidShop))))
                .andExpect(status().isBadRequest());
    }
}
