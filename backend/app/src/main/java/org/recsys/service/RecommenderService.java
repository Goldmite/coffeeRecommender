package org.recsys.service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

import org.recsys.dto.recommendation.Candidate;
import org.recsys.dto.recommendation.SimilarCoffees;
import org.recsys.dto.recommendation.TrainedModel;
import org.recsys.repository.CoffeeRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecommenderService {

    private final CoffeeRepository coffeeRepository;
    private final ModelProvider provider;

    // Find Top N candidates by target - user coffee preference vector
    public List<SimilarCoffees> getSimilarCoffees(float[] target, int n) {
        return coffeeRepository.findTopSimilarCoffeeCandidates(target, n);
    }

    public List<Candidate> getCFCandidates(Long userId, int n) {
        TrainedModel model = provider.getCurrentModel().orElseThrow(() -> new RuntimeException());

        long now = Instant.now().getEpochSecond();

        List<Long> candidateIds = coffeeRepository.findAllIds();
        return candidateIds.stream()
                .map(coffeeId -> {
                    float score = model.predict(userId, coffeeId, now);
                    float nScore = score / 5.0f; // normalize
                    return new Candidate(coffeeId, nScore, "CF");
                })
                .sorted(Comparator.comparing(Candidate::similarity).reversed())
                .limit(n)
                .toList();
    }

}
