package org.recsys.dto.shop;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ShopUpdateRequest {

    @NotNull
    private Integer id;

    @NotBlank
    @Length(max = 96)
    private String name;

    @NotBlank
    @URL
    private String shopUrl;

    @JsonProperty("is_active")
    private Boolean isActive;
}
