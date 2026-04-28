package org.recsys.dto.recommendation;

public record FeatureFilterRequest(
        Float roastWeight,
        Float scaWeight,
        Float acidityWeight,
        Float bodyWeight,
        Float aftertasteWeight,
        Float sweetnessWeight,
        Float bitternessWeight,
        Float singleOriginWeight,
        Float fruityWeight,
        Float floralWeight,
        Float sweetWeight,
        Float nuttyCocoaWeight,
        Float spicesWeight,
        Float sourWeight,
        Float vegetalWeight) {

}
