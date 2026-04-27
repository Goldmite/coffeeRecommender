package org.recsys.dto.coffee;

import java.math.BigDecimal;
import java.util.List;

import org.recsys.dto.shop.ShopResponse;
import org.recsys.model.Processing;
import org.recsys.model.RoastLevel;

import lombok.Value;

@Value
public class CoffeeBeanResponse {

    Long id;
    String name;
    BigDecimal price;
    String productUrl;
    ShopResponse shop;
    // features
    List<String> origins;
    Processing process;
    RoastLevel roastLevel;
    String description;
    List<Integer> altitude;
    Double scaScore;
    Integer acidity;
    Integer body;
    Integer aftertaste;
    Integer sweetness;
    Integer bitterness;
    List<String> flavorNotes;
}
