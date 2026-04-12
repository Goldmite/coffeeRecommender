package org.recsys.dto.shop;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ShopUpdateRequest extends ShopRequest {

    @NotNull
    private Integer id;
}
