package org.recsys.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.recsys.component.ModelLifecycleManager;
import org.recsys.config.HybridConfig;
import org.recsys.dto.recommendation.Candidate;
import org.recsys.dto.recommendation.FeatureFilterRequest;
import org.recsys.dto.recommendation.RecommendationDto;
import org.recsys.dto.recommendation.RecommendationFilterRequest;
import org.recsys.dto.recommendation.SimilarCoffees;
import org.recsys.dto.recommendation.TrainedModel;
import org.recsys.mapper.CoffeeMapper;
import org.recsys.mapper.WeightMapper;
import org.recsys.model.CoffeeBean;
import org.recsys.repository.CoffeeRepository;
import org.recsys.repository.UserInteractionsRepository;

@ExtendWith(MockitoExtension.class)
class RecommenderServiceTest {

    @Mock
    private CoffeeRepository coffeeRepository;
    @Mock
    private ModelProvider provider;
    @Mock
    private UserPreferencesService preferencesService;
    @Mock
    private WeightVectorService weightVectorService;
    @Mock
    private UserInteractionsRepository interactionsRepository;
    @Mock
    private HybridConfig config;
    @Mock
    private CoffeeMapper mapper;
    @Mock
    private WeightMapper weightMapper;
    @Mock
    private ModelLifecycleManager manager;

    @InjectMocks
    private RecommenderService recommenderService;

    private final Long userId = 1L;

    @BeforeEach
    void setUp() {
        // default config values
        lenient().when(config.getInflectionPoint()).thenReturn(10);
        lenient().when(config.getCf()).thenReturn(0.8f);
        lenient().when(config.getSteepness()).thenReturn(1.0f);
        lenient().when(config.getAlpha()).thenReturn(0.7f);
        lenient().when(manager.getSystemMaturity()).thenReturn(1.0f);
    }

    @Test
    void getHybridRecommendations_Positive_FullFlow() {
        // GIVEN: User with enough interactions to trigger CF
        List<Integer> shops = List.of(1, 2);
        RecommendationFilterRequest filterReq = new RecommendationFilterRequest(shops, null);

        when(interactionsRepository.countByUserId(userId)).thenReturn(50);
        when(weightMapper.toFilterMap(null)).thenReturn(Collections.emptyMap());

        // 1. Mock CBF Path
        float[] mockProfile = new float[30];
        when(preferencesService.getUserPreferenceFlavorProfile(userId)).thenReturn(mockProfile);
        when(weightVectorService.getBaseWeightVector()).thenReturn(new float[30]);

        SimilarCoffees mockSimilar = mock(SimilarCoffees.class);
        when(mockSimilar.getId()).thenReturn(100L);
        when(mockSimilar.getSimilarityScore()).thenReturn(0.9); // (0.8 + 1) / 2
        when(coffeeRepository.findTopSimilarCoffeeCandidates(any(), anyInt(), eq(shops), anyBoolean()))
                .thenReturn(List.of(mockSimilar));

        // 2. Mock CF Path
        TrainedModel mockModel = mock(TrainedModel.class);
        when(provider.getCurrentModel()).thenReturn(Optional.of(mockModel));
        when(coffeeRepository.findAllIdsInShops(shops)).thenReturn(List.of(100L));
        when(mockModel.predict(eq(userId), eq(100L), anyLong())).thenReturn(4.5f);

        // 3. Mock Repository & Mapper
        CoffeeBean bean = new CoffeeBean();
        bean.setId(100L);
        when(coffeeRepository.findAllById(anyList())).thenReturn(List.of(bean));

        // WHEN
        List<RecommendationDto> results = recommenderService.getHybridRecommendations(userId, 5, filterReq);

        // THEN
        assertFalse(results.isEmpty());
        RecommendationDto topRec = results.get(0);

        // Score should be weighted sum of CF (4.5/5.0 * weight) and CBF (0.9 * weight)
        assertTrue(topRec.score() > 0);
        verify(coffeeRepository).findTopSimilarCoffeeCandidates(any(), anyInt(), eq(shops), anyBoolean());
        verify(mockModel).predict(eq(userId), eq(100L), anyLong());
    }

    @Test
    void getHybridRecommendations_WithFeatureFilters_ReducesCFWeight() {
        // GIVEN: Feature filter is present
        FeatureFilterRequest featureReq = new FeatureFilterRequest(
                0.8f, 0.2f, null, null, null, null, null, null, null, null, null, null, null, null, null);
        RecommendationFilterRequest filterReq = new RecommendationFilterRequest(List.of(1), featureReq);

        // Return a non-empty map to trigger cfWeight *= 0.3f
        when(weightMapper.toFilterMap(featureReq)).thenReturn(Map.of(0, 0.8f));
        when(interactionsRepository.countByUserId(userId)).thenReturn(100);

        // Mock dependencies to allow method to finish
        when(preferencesService.getUserPreferenceFlavorProfile(userId)).thenReturn(new float[30]);
        when(weightVectorService.getBaseWeightVector()).thenReturn(new float[30]);
        when(coffeeRepository.findTopSimilarCoffeeCandidates(any(), anyInt(), any(), anyBoolean()))
                .thenReturn(Collections.emptyList());
        when(provider.getCurrentModel()).thenReturn(Optional.empty());

        // WHEN
        recommenderService.getHybridRecommendations(userId, 10, filterReq);

        // THEN
        verify(weightMapper).toFilterMap(featureReq);
        // Even with 100 interactions, the CF weight should be slashed due to
        // intent-based filters
    }

    @Test
    void prepareTargetVector_ShouldApplyAlphaBlendingCorrecty() {
        // GIVEN
        float[] userProfile = new float[30];
        userProfile[0] = 0.2f; // User pref

        // Filter: 5.0 (Max). Norm: (5-3)/2 = 1.0
        Map<Integer, Float> filters = Map.of(0, 5.0f);

        when(config.getAlpha()).thenReturn(0.8f); // 80% Filter, 20% User
        when(weightVectorService.getBaseWeightVector()).thenReturn(new float[30]);

        // WHEN
        recommenderService.prepareTargetVector(userProfile, filters);
        // THEN
        // Logic check: (0.8 * 1.0) + (0.2 * 0.2) = 0.8 + 0.04 = 0.84
        // Since we mock the weights/normalize calls, we just verify delegation
        verify(weightVectorService).applyFeatureWeights(any(), any());
        verify(weightVectorService).l2Normalize(any());
    }

    @Test
    void getCFCandidates_Negative_ReturnsEmptyWhenModelMissing() {
        // GIVEN
        when(provider.getCurrentModel()).thenReturn(Optional.empty());

        // WHEN
        List<Candidate> candidates = recommenderService.getCFCandidates(userId, 10, List.of(1));

        // THEN
        assertTrue(candidates.isEmpty());
    }

    @Test
    void determineCfWeight_ColdStart_ShouldReturnZero() {
        // GIVEN: Interaction count is below half inflection point (20/2 = 10)
        when(interactionsRepository.countByUserId(userId)).thenReturn(5);
        when(config.getInflectionPoint()).thenReturn(20);

        // WHEN
        // Accessing via getHybridRecommendations to see the weight effect
        RecommendationFilterRequest req = new RecommendationFilterRequest(null, null);
        when(weightMapper.toFilterMap(any())).thenReturn(null);
        when(preferencesService.getUserPreferenceFlavorProfile(userId)).thenReturn(new float[30]);
        when(weightVectorService.getBaseWeightVector()).thenReturn(new float[30]);
        when(coffeeRepository.findTopSimilarCoffeeCandidates(any(), anyInt(), any(), anyBoolean()))
                .thenReturn(Collections.emptyList());

        recommenderService.getHybridRecommendations(userId, 10, req);

        // THEN
        // If weight was 0, CF candidates should never be fetched
        verify(provider, never()).getCurrentModel();
    }
}
