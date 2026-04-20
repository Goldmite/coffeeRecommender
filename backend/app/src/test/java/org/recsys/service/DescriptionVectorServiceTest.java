package org.recsys.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.recsys.config.FeatureWeights;
import org.recsys.repository.CoffeeRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class DescriptionVectorServiceTest {

    @Mock
    private CoffeeRepository repository;

    private DescriptionVectorService service;

    private List<String> mockDescriptions;
    private FeatureWeights settings;

    @BeforeEach
    void setUp() {
        settings = new FeatureWeights(0.15f);
        service = new DescriptionVectorService(repository, settings);
        mockDescriptions = Arrays.asList(
                "There is dark chocolate and nuts",
                "This coffee has citrus and berries notes",
                "Chocolate and flowers");
    }

    @Test
    void shouldInitializeIdfMap_whenInitCalled() {
        // given
        when(repository.findAllDescriptions()).thenReturn(mockDescriptions);
        // when
        service.initInverseDocumentFrequency();
        // then
        verify(repository, times(1)).findAllDescriptions();
        // Indirectly verify IDF calculation via vector results
        // "chocolate" (index 0) appears in 2/3 docs.
        // idf = ln(3 / (2+1)) + 1 = 1.0.
        // TF for input "chocolate" = 1.0. Result = 1.0 * 1.0 * 0.15 = 0.15
        float[] vector = service.getDescriptionTfIdfVector("chocolate");
        assertEquals(0.15f, vector[0], 0.001f);
    }

    @Test
    void shouldMatchVocabularySize() {
        // given
        when(repository.findAllDescriptions()).thenReturn(mockDescriptions);
        service.initInverseDocumentFrequency();
        // when
        float[] vector = service.getDescriptionTfIdfVector("This is a chocolate citrus coffee");
        // then
        assertEquals(11, vector.length);
    }

    @Test
    void shouldApplyWeightAndCalculateCorrectValues() {
        // given
        when(repository.findAllDescriptions()).thenReturn(mockDescriptions);
        service.initInverseDocumentFrequency();
        // when
        float[] vector = service.getDescriptionTfIdfVector("chocolate");
        // then
        assertEquals(0.15f, vector[0], 0.001f); // chocolate
        assertEquals(0.0f, vector[1], 0.001f);
    }

    @Test
    void shouldHandleCaseAndPunctuationDuringTokenization() {
        // given
        when(repository.findAllDescriptions()).thenReturn(mockDescriptions);
        service.initInverseDocumentFrequency();
        // when
        float[] vector = service.getDescriptionTfIdfVector("CHOCOLATE!");
        // then
        assertTrue(vector[0] > 0);
    }

    @Test
    void shouldReturnEmptyVector_whenRepositoryIsEmpty() {
        // given
        when(repository.findAllDescriptions()).thenReturn(List.of());
        service.initInverseDocumentFrequency();
        // when
        float[] vector = service.getDescriptionTfIdfVector("chocolate");
        // then
        // If idfMap is empty, getOrDefault(term, 1.0) * TF * weight
        // TF = 1.0, IDF = 1.0, weight = 0.15
        assertEquals(0.15f, vector[0], 0.001f);
    }
}