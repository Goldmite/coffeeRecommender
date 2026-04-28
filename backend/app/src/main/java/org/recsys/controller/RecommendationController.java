package org.recsys.controller;

import java.util.List;

import org.recsys.dto.recommendation.FeatureFilterRequest;
import org.recsys.dto.recommendation.RecommendationDto;
import org.recsys.service.RecommenderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/recommendation")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommenderService service;

    @GetMapping
    public ResponseEntity<List<RecommendationDto>> getHybridRecommendations(@RequestParam Long userId,
            @RequestParam(defaultValue = "10") int limit, @RequestBody(required = false) FeatureFilterRequest filters) {
        return ResponseEntity.ok(service.getHybridRecommendations(userId, limit, filters));
    }

}
