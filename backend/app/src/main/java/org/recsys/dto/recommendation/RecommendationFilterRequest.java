package org.recsys.dto.recommendation;

import java.util.List;

public record RecommendationFilterRequest(
        List<Integer> shopIds,
        FeatureFilterRequest featureFilter) {
}
