package org.recsys.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.recsys.repository.CoffeeRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class DescriptionVectorServiceTest {

    @Mock
    private CoffeeRepository repository;

    @InjectMocks
    private DescriptionVectorService service;

    private List<String> mockDescriptions;

    @BeforeEach
    void setUp() {
        mockDescriptions = Arrays.asList(
                "There are chocolate and nuts",
                "This coffee has citrus and berries notes",
                "Chocolate and flowers");
    }

    @Test
    void shouldReturnCorrectMap_whenCalculateGlobalIdf() {
        // when
        Map<String, Double> idfs = service.calculateGlobalIdf(mockDescriptions);
        // then
        assertNotNull(idfs);
        assertTrue(idfs.containsKey("chocolate"));
        assertTrue(idfs.containsKey("berries"));
        // "chocolate" appears in 2 out of 3 docs.
        // idf = ln(3 / (2+1)) + 1 = ln(1) + 1 = 1.0
        assertEquals(1.0, idfs.get("chocolate"), 0.01);
    }

    @Test
    void shouldGetDescriptionTfIdfVectorAndMatchVocabularySize() {
        // given
        Map<String, Double> idfs = service.calculateGlobalIdf(mockDescriptions);
        String input = "This is a chocolate citrus coffee";
        // when
        float[] vector = service.getDescriptionTfIdfVector(input, idfs);
        // then
        assertEquals(7, vector.length);
    }

    @Test
    void shouldApplyWeightToVectorValues() {
        // given
        Map<String, Double> idfs = service.calculateGlobalIdf(mockDescriptions);
        // when
        // "chocolate" is at index 0 in vocabulary.
        // Tokens: [chocolate] (size 1). TF = 1/1 = 1.0. IDF = 1.0.
        // Expected: 1.0 * 1.0 * 0.15 = 0.15
        float[] vector = service.getDescriptionTfIdfVector("chocolate", idfs);
        // then
        assertEquals(0.15f, vector[0], 0.001f);
        assertEquals(0.0f, vector[1], 0.001f); // "berries" should be 0
    }

    @Test
    void shouldGetDescriptionsOnInit() {
        // given
        when(repository.findAllDescriptions()).thenReturn(mockDescriptions);
        // when
        service.initInverseDocumentFrequency();
        // then
        verify(repository, times(1)).findAllDescriptions();
    }

    @Test
    void shouldHandleCaseAndPuncatuationDuringTokenization() {
        // given
        Map<String, Double> idfs = service.calculateGlobalIdf(mockDescriptions);
        // when
        float[] vector = service.getDescriptionTfIdfVector("CHOCOLATE!", idfs);
        // then
        assertTrue(vector[0] > 0);
    }
}