package org.recsys.dto.recommendation;

public record Candidate(
        Long id,
        float similarity,
        String source // CF or CBF
) {
}
