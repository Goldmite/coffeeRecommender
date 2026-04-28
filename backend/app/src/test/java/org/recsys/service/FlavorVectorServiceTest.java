package org.recsys.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.recsys.model.FlavorCategory;
import org.recsys.repository.CoffeeRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class FlavorVectorServiceTest {

    @Mock
    private CoffeeRepository repository;

    private FlavorVectorService service;
    private List<String> mockDescriptions;

    @BeforeEach
    void setUp() {
        service = new FlavorVectorService(repository);
        mockDescriptions = Arrays.asList(
                "There is dark chocolate and nuts",
                "This coffee has citrus and berries notes",
                "Chocolate and flowers");
    }

    @Test
    void shouldInitializeIdfMapAndCalculateCorrectVectorSize() {
        // given
        when(repository.findAllDescriptions()).thenReturn(mockDescriptions);
        // when
        service.initInverseDocumentFrequency();
        float[] vector = service.getUnifiedFlavorVector(null, "Just a coffee");
        // then
        verify(repository, times(1)).findAllDescriptions();
        assertEquals(FlavorCategory.values().length, vector.length);
    }

    @Test
    void shouldGivePriorityToExplicitFlavorNotes() {
        // given
        when(repository.findAllDescriptions()).thenReturn(mockDescriptions);
        service.initInverseDocumentFrequency();
        // when: "fruit" is in description (soft) but "berry" is in notes (hard)
        List<String> explicitNotes = List.of("berry");
        String description = "This coffee has some fruit aftertaste";
        float[] vector = service.getUnifiedFlavorVector(explicitNotes, description);
        // then
        assertEquals(1.0f, vector[FlavorCategory.FRUITY.ordinal()], 0.001f,
                "Should match berry as FRUITY with full weight");
    }

    @Test
    void shouldHandleCaseAndPunctuationDuringTokenization() {
        // given
        when(repository.findAllDescriptions()).thenReturn(mockDescriptions);
        service.initInverseDocumentFrequency();
        // when
        float[] vector = service.getUnifiedFlavorVector(List.of("CHERRY"), "CHOCOLATE!");
        // then
        assertTrue(vector[FlavorCategory.FRUITY.ordinal()] == 1.0f, "Should match CHERRY as FRUITY");
        assertTrue(vector[FlavorCategory.NUTTYCOCOA.ordinal()] > 0, "Should match CHOCOLATE from description");
    }

    @Test
    void shouldReturnZerosForUnknownFlavors() {
        // given
        when(repository.findAllDescriptions()).thenReturn(mockDescriptions);
        service.initInverseDocumentFrequency();
        // when
        float[] vector = service.getUnifiedFlavorVector(List.of(), "Standard funkylishous tasting coffee");
        // then
        for (float feat : vector) {
            assertEquals(0.0f, feat, 0.001f);
        }
    }
}