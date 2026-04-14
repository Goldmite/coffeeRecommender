package org.recsys.dto.coffee;

import java.math.BigDecimal;
import java.util.List;

import org.recsys.model.CoffeeBean;
import org.recsys.model.CoffeeFeatures;
import org.recsys.model.RoastLevel;

import io.hypersistence.utils.hibernate.type.range.Range;
import lombok.Data;

@Data
public class CoffeeBeanRequest {

    private String name;
    private BigDecimal price;
    private String productUrl;
    private Integer shopId;
    private FeaturesRequest features;

    @Data
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

    public CoffeeBean toEntity() {
        CoffeeBean bean = new CoffeeBean();
        bean.setName(this.name);
        bean.setPrice(this.price);
        bean.setProductUrl(this.productUrl);
        bean.setShopId(this.shopId);

        if (this.features != null) {
            CoffeeFeatures f = new CoffeeFeatures();
            f.setOrigins(this.features.getOrigins());
            f.setProcess(this.features.getProcess());
            f.setRoastLevel(this.features.getRoastLevel());
            f.setScaScore(this.features.getScaScore());
            f.setFlavorNotes(this.features.getFlavorNotes());
            if (this.features.getAltitude() != null) {
                f.setAltitude(
                        Range.closed(this.features.getAltitude().getFirst(), this.features.getAltitude().getLast()));
            }
            f.setCoffeeBean(bean);
            bean.setFeatures(f);
        }

        return bean;
    }
}