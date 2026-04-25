package org.recsys.dto.recommendation;

public interface SimilarCoffees {
    Long getId();

    Double getDistance();

    default Double getSimilarityScore() {
        if (getDistance() == null)
            return 0.0;
        return Math.max(0, 1.0 - getDistance());
    }
}
