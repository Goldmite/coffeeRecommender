package org.recsys.dto.coffee;

import java.util.List;

import io.hypersistence.utils.hibernate.type.range.Range;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CoffeeVectorizationDto {
    private Long coffeeId;
    private List<String> origins;
    private String process;
    private int roastLevel;
    private String description;
    private Range<Integer> altitude;
    private double scaScore;
    private int acidity;
    private int body;
    private int aftertaste;
    private int sweetness;
    private int bitterness;
    private List<String> flavorNotes;

    public boolean isSingleOrigin() {
        return origins != null && origins.size() == 1;
    }
}
