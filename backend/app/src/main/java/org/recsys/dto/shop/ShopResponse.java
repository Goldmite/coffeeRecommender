package org.recsys.dto.shop;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ShopResponse {

    private Integer id;
    private String name;
    private String shopUrl;

    @JsonProperty("is_active")
    private Boolean isActive;
}
