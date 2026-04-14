package org.recsys.dto.coffee;

import java.math.BigDecimal;
import java.util.List;

import org.recsys.model.RoastLevel;

import lombok.Value;

@Value
public class CoffeeBeanResponse {

    Long id;
    String name;
    BigDecimal price;
    String productUrl;
    int shopId;
    // features
    List<String> origins;
    String process;
    RoastLevel roastLevel;
    // float[] description;
    List<Integer> altitude;
    Double scaScore;
    Integer acidity;
    Integer body;
    Integer aftertaste;
    Integer sweetness;
    Integer bitterness;
    List<String> flavorNotes;
    // float[] flavorVector;
}
