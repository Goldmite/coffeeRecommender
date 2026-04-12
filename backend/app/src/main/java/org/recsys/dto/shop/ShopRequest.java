package org.recsys.dto.shop;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;
import org.recsys.model.Shop;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ShopRequest {

    @NotBlank
    @Length(max = 96)
    private String name;

    @NotBlank
    @URL
    private String shopUrl;

    @JsonProperty("is_active")
    private Boolean isActive = false;

    public Shop toEntity() {
        Shop shop = new Shop();
        shop.setName(name);
        shop.setShopUrl(shopUrl);
        shop.setIsActive(isActive);
        return shop;
    }
}
