package org.recsys.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.recsys.dto.coffee.CoffeeVectorizationDto;
import org.recsys.model.Processing;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CoffeeVectorService {

    private final FlavorVectorService flavorVectorService;

    private static final List<String> ORIGINS = List.of("Brazil", "Colombia", "Ethiopia", "Peru", "Kenya", "Nicaragua",
            "Guatemala", "Indonesia", "India", "Other");

    // normalize using min max to range [0, 1]
    private float normalize(double x, double min, double max) {
        if (Double.compare(min, max) == 0) {
            return 0.5f;
        }
        // min-max with range [0, 1]: (x - min) / (max - min)
        double normalized = (x - min) / (max - min);
        normalized = Math.max(0, Math.min(1, normalized)); // precaution for out of bounds
        return (float) normalized;
    }

    public float[] createFlavorVector(CoffeeVectorizationDto dto) {
        // Normalize simple attributes
        float[] normalizedPart = normalizeSimpleAttributes(dto);
        // Encoding categories (origins, process)
        float[] processPart = multiHotEncode(Collections.singletonList(dto.getProcess()),
                Arrays.stream(Processing.values()).map(Processing::getProcess).toList());
        float[] originPart = multiHotEncode(dto.getOrigins(), ORIGINS);
        // Flavors derived from Notes and fallback to Description derived features
        float[] flavorPart = flavorVectorService.getUnifiedFlavorVector(dto.getFlavorNotes(), dto.getDescription());
        // Combine vectors
        return combine(normalizedPart, processPart, originPart, flavorPart);
    }

    private float[] normalizeSimpleAttributes(CoffeeVectorizationDto dto) {
        float nRoast = normalize(dto.getRoastLevel(), 1, 5);
        float nAltitude = normalize((dto.getAltitude().lower() + dto.getAltitude().upper()) / 2.0f, 1000, 2501);
        float nScaScore = normalize(dto.getScaScore(), 80, 100);
        float nAcidity = normalize(dto.getAcidity(), 1, 10);
        float nBody = normalize(dto.getBody(), 1, 10);
        float nAftertaste = normalize(dto.getAftertaste(), 1, 10);
        float nSweetness = normalize(dto.getSweetness(), 1, 10);
        float nBitterness = normalize(dto.getBitterness(), 1, 10);

        float singleOrigin = dto.isSingleOrigin() ? 1.0f : 0.0f;

        return new float[] { nRoast, nAltitude, nScaScore, nAcidity, nBody, nAftertaste, nSweetness, nBitterness,
                singleOrigin };
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
