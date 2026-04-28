package org.recsys.mapper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.recsys.dto.recommendation.FeatureFilterRequest;
import org.springframework.stereotype.Component;

@Component
public class WeightMapper {

    public Map<Integer, Float> toFilterMap(FeatureFilterRequest dto) {
        if (dto == null)
            return Collections.emptyMap();

        Map<Integer, Float> filters = new HashMap<>();

        // Attributes (0-8)
        addIfNotNull(filters, 0, dto.roastWeight());
        // index: 1 altitude
        addIfNotNull(filters, 2, dto.scaWeight());
        addIfNotNull(filters, 3, dto.acidityWeight());
        addIfNotNull(filters, 4, dto.bodyWeight());
        addIfNotNull(filters, 5, dto.aftertasteWeight());
        addIfNotNull(filters, 6, dto.sweetnessWeight());
        addIfNotNull(filters, 7, dto.bitternessWeight());
        addIfNotNull(filters, 8, dto.singleOriginWeight());

        // Flavor Categories (23-29)
        addIfNotNull(filters, 23, dto.fruityWeight());
        addIfNotNull(filters, 24, dto.floralWeight());
        addIfNotNull(filters, 25, dto.sweetWeight());
        addIfNotNull(filters, 26, dto.nuttyCocoaWeight());
        addIfNotNull(filters, 27, dto.spicesWeight());
        addIfNotNull(filters, 28, dto.vegetalWeight());
        addIfNotNull(filters, 29, dto.sourWeight());

        return filters;
    }

    private void addIfNotNull(Map<Integer, Float> map, int index, Float value) {
        if (value != null) {
            map.put(index, value);
        }
    }
}
