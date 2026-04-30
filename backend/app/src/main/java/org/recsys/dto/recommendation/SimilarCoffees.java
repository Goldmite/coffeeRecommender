package org.recsys.dto.recommendation;

public interface SimilarCoffees {
    Long getId();

    Double getSimilarity();

    default Double getSimilarityScore() {
        if (getSimilarity() == null)
            return 0.0;
        return (getSimilarity() + 1.0) / 2.0;
    }
}
