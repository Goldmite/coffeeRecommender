package org.recsys.dto.shop;

import org.recsys.model.Shop;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ShopResponse {

    Integer id;
    String name;
    String shopUrl;

    @JsonProperty("is_active")
    Boolean isActive;

    public static ShopResponse fromEntity(Shop shop) {
        return ShopResponse.builder()
                .id(shop.getId())
                .name(shop.getName())
                .shopUrl(shop.getShopUrl())
                .isActive(shop.getIsActive())
                .build();
    }
}
