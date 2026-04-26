package org.recsys.service;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.recsys.config.HybridConfig;
import org.recsys.dto.recommendation.Candidate;
import org.recsys.dto.recommendation.RecommendationDto;
import org.recsys.dto.recommendation.SimilarCoffees;
import org.recsys.dto.recommendation.TrainedModel;
import org.recsys.mapper.CoffeeMapper;
import org.recsys.model.CoffeeBean;
import org.recsys.repository.CoffeeRepository;
import org.recsys.repository.UserInteractionsRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecommenderService {

    private final CoffeeRepository coffeeRepository;
    private final ModelProvider provider;
    private final UserPreferencesService preferencesService;
    private final UserInteractionsRepository interactionsRepository;
    private final HybridConfig config;
    private final CoffeeMapper mapper;

    public List<RecommendationDto> getHybridRecommendations(Long userId, int limit) {
        int candidateLimit = Math.min(Math.max(limit * 15, 50), 150); // 15x display limit in range [50-150]
        List<Candidate> cfCandidates = getCFCandidates(userId, candidateLimit);
        List<Candidate> cbfCandidates = getCBFCandidates(userId, candidateLimit);

        float cfWeight = determineCfWeight(userId);
        float cbfWeight = 1 - cfWeight;
        Map<Long, Float> hybridScores = new HashMap<>();
        // add CF results
        for (Candidate cf : cfCandidates) {
            hybridScores.put(cf.id(), cf.similarity() * cfWeight);
        }

        // add CBF results
        for (Candidate cbf : cbfCandidates) {
            float currentScore = hybridScores.getOrDefault(cbf.id(), 0f);
            float weightedCbfScore = cbf.similarity() * cbfWeight;

            hybridScores.put(cbf.id(), currentScore + weightedCbfScore);
        }
        // rank
        List<Long> rankedIds = hybridScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Float>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .toList();

        Map<Long, CoffeeBean> coffeeMap = coffeeRepository.findAllById(rankedIds)
                .stream().collect(Collectors.toMap(CoffeeBean::getId, c -> c));
        // keep sorted order
        return rankedIds.stream()
                .filter(coffeeMap::containsKey)
                .map(id -> new RecommendationDto(
                        mapper.toResponse(coffeeMap.get(id)),
                        hybridScores.get(id)))
                .toList();
    }

    // Find Top N candidates by target - user coffee preference vector
    public List<Candidate> getCBFCandidates(Long userId, int n) {
        float[] target = preferencesService.getUserPreferenceFlavorProfile(userId);

        List<SimilarCoffees> coffees = coffeeRepository.findTopSimilarCoffeeCandidates(target, n);

        return coffees.stream().map(c -> new Candidate(c.getId(), c.getSimilarityScore().floatValue(), "CBF")).toList();
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

    private float determineCfWeight(Long userId) {
        int interactionCount = interactionsRepository.countByUserId(userId);
        // cold start (new user)
        if (interactionCount < config.getInflectionPoint() / 2) // half the inflection point
            return 0.0f; // fallback to CBF
        // Sigmoid - cf_max / (1 + e^(-k*(x - x0)))
        float dynamicWeight = (float) (config.getCf()
                / (1 + Math.exp(-config.getSteepness() * (interactionCount - config.getInflectionPoint()))));

        return dynamicWeight;
    }

}
