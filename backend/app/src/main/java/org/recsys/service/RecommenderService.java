package org.recsys.service;

import java.util.List;

import org.recsys.dto.recommendation.CoffeeCandidate;
import org.recsys.repository.CoffeeRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecommenderService {

    private final CoffeeRepository coffeeRepository;

    // Find Top N candidates by target - user coffee preference vector
    public List<CoffeeCandidate> getSimilarCoffees(float[] target, int n) {
        return coffeeRepository.findTopSimilarCoffeeCandidates(target, n);
    }
}
