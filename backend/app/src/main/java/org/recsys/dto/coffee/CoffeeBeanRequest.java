package org.recsys.dto.coffee;

import java.math.BigDecimal;
import java.util.List;

import org.recsys.model.RoastLevel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoffeeBeanRequest {

    private String name;
    private BigDecimal price;
    private String productUrl;
    private Integer shopId;
    private FeaturesRequest features;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeaturesRequest {

        private List<String> origins;
        private String process;
        private RoastLevel roastLevel;
        private List<Integer> altitude;
        private Double scaScore;
        private Integer acidity;
        private Integer body;
        private Integer aftertaste;
        private Integer sweetness;
        private Integer bitterness;
        private List<String> flavorNotes;
    }
}