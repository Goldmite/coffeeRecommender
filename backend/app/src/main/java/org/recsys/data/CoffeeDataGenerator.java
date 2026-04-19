package org.recsys.data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.recsys.dto.coffee.CoffeeBeanRequest;
import org.recsys.model.RoastLevel;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CoffeeDataGenerator {

    private final Random random;

    private static final List<String> ORIGINS = List.of("Brazil", "Colombia", "Ethiopia", "Peru", "Kenya", "Nicaragua",
            "Guatemala", "Indonesia", "India");
    private static final List<String> PROCESSES = List.of("Washed", "Natural");
    // Mapping roast levels to a broad pool of flavor notes
    private static final Map<RoastLevel, List<String>> FLAVOR_MAP = Map.of(
            RoastLevel.LIGHT, List.of("Jasmine", "Bergamot", "Peach", "Earl Grey", "Lemon", "Green Apple", "Floral"),
            RoastLevel.MEDIUM_LIGHT, List.of("Blueberry", "Strawberry", "Honey", "Apricot", "Orange", "Raspberry"),
            RoastLevel.MEDIUM, List.of("Milk Chocolate", "Caramel", "Hazelnut", "Red Apple", "Nougat", "Pear"),
            RoastLevel.MEDIUM_DARK, List.of("Chocolate", "Almond", "Toffee", "Brown Sugar", "Plum", "Raisin"),
            RoastLevel.DARK, List.of("Dark Chocolate", "Molasses", "Smoke", "Roasted Walnut", "Clove", "Cacao Nibs"));

    public CoffeeBeanRequest generate(int index) {
        String coffeeNr = String.format("%04d", index);
        RoastLevel roast = generateRoast();
        List<String> origin = generateOrigins();
        // coffee features
        CoffeeBeanRequest.FeaturesRequest features = CoffeeBeanRequest.FeaturesRequest.builder()
                .origins(generateOrigins())
                .process(pickRandom(PROCESSES))
                .roastLevel(roast)
                .altitude(generateAltitude())
                .scaScore(generateScaScore())
                .acidity(pickFromscaleOneToTen())
                .body(pickFromscaleOneToTen())
                .aftertaste(pickFromscaleOneToTen())
                .sweetness(pickFromscaleOneToTen())
                .bitterness(pickFromscaleOneToTen())
                .flavorNotes(generateNotes(roast, 4))
                .build();
        // coffee bean
        return CoffeeBeanRequest.builder()
                .name(origin.getFirst() + " Coffee #" + coffeeNr)
                .price(BigDecimal.valueOf(15.0 + (random.nextDouble() * 20.0)).setScale(2, RoundingMode.HALF_UP))
                .productUrl("https://example.com/coffee/" + coffeeNr)
                .shopId(random.nextInt(5) + 1)
                .features(features)
                .build();
    }

    private List<String> generateOrigins() {
        List<String> origins = new ArrayList<>();
        origins.add(pickRandom(ORIGINS));
        if (random.nextInt(100) < 15) {
            String anotherOrigin = pickRandom(ORIGINS);
            if (!origins.contains(anotherOrigin)) {
                origins.add(anotherOrigin);
            }
        }
        return origins;
    }

    // Weighted roast level generation, mostly medium
    private RoastLevel generateRoast() {
        int roll = random.nextInt(100);
        if (roll < 10)
            return RoastLevel.LIGHT;
        if (roll < 25)
            return RoastLevel.MEDIUM_LIGHT;
        if (roll < 75)
            return RoastLevel.MEDIUM;
        if (roll < 90)
            return RoastLevel.MEDIUM_DARK;
        return RoastLevel.DARK;
    }

    private List<Integer> generateAltitude() {
        int lower = 1000 + random.nextInt(7) * 100;
        int upper = lower + random.nextInt(8) * 100;
        return List.of(lower, upper);
    }

    private List<String> generateNotes(RoastLevel roast, int count) {
        List<String> map = FLAVOR_MAP.get(roast);
        // variance
        List<String> shuffled = new ArrayList<>(map);
        Collections.shuffle(shuffled);
        return shuffled.subList(0, Math.min(count, shuffled.size()));
    }

    private Double generateScaScore() {
        double mean = 83.0;
        double stdDeviation = 2.5;
        double score = mean + (random.nextGaussian() * stdDeviation);
        // fit to expected range
        score = Math.max(80.0, Math.min(98.0, score));
        return Math.round(score * 100.0) / 100.0;
    }

    private Integer pickFromscaleOneToTen() {
        return random.nextInt(10) + 1;
    }

    private <T> T pickRandom(List<T> list) {
        return list.get(random.nextInt(list.size()));
    }

}
