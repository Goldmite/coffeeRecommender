package org.recsys.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.recsys.dto.coffee.CoffeeVectorizationDto;
import org.recsys.model.Processing;
import org.recsys.model.RoastLevel;
import org.springframework.stereotype.Service;

import io.hypersistence.utils.hibernate.type.range.Range;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CoffeeVectorService {

    private final FlavorVectorService flavorVectorService;
    private final WeightVectorService weightVectorService;

    private static final List<String> ORIGINS = List.of("Brazil", "Colombia", "Ethiopia", "Peru", "Kenya", "Nicaragua",
            "Guatemala", "Indonesia", "India", "Other");

    public float[] createBaseVector() {
        CoffeeVectorizationDto defaultDto = CoffeeVectorizationDto.builder()
                .process(Processing.NATURAL.getProcess())
                .roastLevel(RoastLevel.MEDIUM.ordinal())
                .altitude(Range.closed(1000, 2500))
                .acidity(5)
                .body(5)
                .aftertaste(5)
                .sweetness(5)
                .bitterness(5)
                .build();
        return createFlavorVector(defaultDto);
    }

    public float[] createFlavorVector(CoffeeVectorizationDto dto) {
        // 1. Normalize simple attributes
        float[] normalizedPart = normalizeSimpleAttributes(dto);
        // Encoding categories (origins, process)
        float[] processPart = multiHotEncode(Collections.singletonList(dto.getProcess()),
                Arrays.stream(Processing.values()).map(Processing::getProcess).toList());
        float[] originPart = multiHotEncode(dto.getOrigins(), ORIGINS);
        // Flavors derived from Notes and fallback to Description derived features
        float[] flavorPart = flavorVectorService.getUnifiedFlavorVector(dto.getFlavorNotes(), dto.getDescription());
        // Combine vectors
        float[] combined = combine(normalizedPart, processPart, originPart, flavorPart);
        // 2. Apply feature weights
        weightVectorService.applyFeatureWeights(combined, weightVectorService.getBaseWeightVector());
        // 3. Normalize whole vector
        return weightVectorService.l2Normalize(combined);
    }

    private float[] normalizeSimpleAttributes(CoffeeVectorizationDto dto) {
        float nRoast = normalize(dto.getRoastLevel(), 0, 4);
        float nAltitude = normalize((dto.getAltitude().lower() + dto.getAltitude().upper()) / 2.0f, 1000, 2501);
        float nScaScore = normalize(dto.getScaScore(), 80, 100);
        float nAcidity = normalize(dto.getAcidity(), 1, 10);
        float nBody = normalize(dto.getBody(), 1, 10);
        float nAftertaste = normalize(dto.getAftertaste(), 1, 10);
        float nSweetness = normalize(dto.getSweetness(), 1, 10);
        float nBitterness = normalize(dto.getBitterness(), 1, 10);

        float singleOrigin = dto.isSingleOrigin() ? 1.0f : -1.0f;

        return new float[] { nRoast, nAltitude, nScaScore, nAcidity, nBody, nAftertaste, nSweetness, nBitterness,
                singleOrigin };
    }

    // normalize using min max to range [-1, 1]
    private float normalize(double x, double min, double max) {
        if (Double.compare(min, max) == 0) {
            return 0;
        }
        // min-max with range [-1, 1]: -1 + ((x - min)(2) / (max - min))
        double normalized = -1 + (((x - min) * 2) / (max - min));
        normalized = Math.max(-1, Math.min(1, normalized)); // precaution for out of bounds
        return (float) normalized;
    }

    private float[] multiHotEncode(List<String> values, List<String> allValues) {
        float[] vector = new float[allValues.size()];
        if (values == null || values.isEmpty())
            return vector;

        for (int i = 0; i < allValues.size(); i++) {
            String attr = allValues.get(i);
            vector[i] = values.stream().anyMatch(v -> v.equalsIgnoreCase(attr)) ? 1.0f : 0.0f;
        }
        return vector;
    }

    private float[] combine(float[]... vectors) {
        int totalLength = 0;
        for (float[] vector : vectors) {
            if (vector != null) {
                totalLength += vector.length;
            }
        }
        float[] combined = new float[totalLength];
        int destPos = 0;
        for (float[] vector : vectors) {
            if (vector != null) {
                System.arraycopy(vector, 0, combined, destPos, vector.length);
                destPos += vector.length;
            }
        }
        return combined;
    }
}
