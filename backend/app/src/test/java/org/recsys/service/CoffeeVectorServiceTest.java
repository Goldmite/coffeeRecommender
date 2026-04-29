package org.recsys.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.recsys.dto.coffee.CoffeeVectorizationDto;
import org.recsys.model.FlavorCategory;

import io.hypersistence.utils.hibernate.type.range.Range;

@ExtendWith(MockitoExtension.class)
class CoffeeVectorServiceTest {

    @Mock
    private FlavorVectorService flavorVectorService;

    @InjectMocks
    private CoffeeVectorService coffeeVectorService;

    private CoffeeVectorizationDto baseDto;

    private static int NORM_PART_SIZE = 9;
    private static int PROCESS_PART_SIZE = 4;
    private static int ORIGIN_PART_SIZE = 10;
    private static int FLAVOR_PART_SIZE = 7;

    @BeforeEach
    void setUp() {
        baseDto = CoffeeVectorizationDto.builder()
                .coffeeId(1L)
                .origins(List.of("Ethiopia"))
                .process("WASHED")
                .roastLevel(2) // MEDIUM
                .description("A floral and fruity coffee")
                .altitude(Range.closed(1000, 2500))
                .scaScore(90.0)
                .acidity(5)
                .body(5)
                .aftertaste(5)
                .sweetness(5)
                .bitterness(5)
                .flavorNotes(List.of("Jasmine", "Lemon"))
                .build();
    }

    @Test
    void shouldNormalizeSimpleAttributesCorrectly() {
        // given
        float[] mockFlavor = new float[FlavorCategory.values().length];
        when(flavorVectorService.getUnifiedFlavorVector(any(), any())).thenReturn(mockFlavor);
        // when
        float[] result = coffeeVectorService.createFlavorVector(baseDto);
        // then: First 9 elements are simple attributes
        // Roast: (3-1)/(5-1) = 0.5
        assertEquals(0.5f, result[0], 0.001f);
        // Altitude: (1750.5-1000)/(2501-1000) = 750.5/1501 = 0.5
        assertEquals(0.5f, result[1], 0.001f);
        // Single Origin boolean
        assertEquals(1.0f, result[8], 0.001f);
    }

    @Test
    void shouldMultiHotEncodeOriginsCorrectly() {
        // given
        baseDto.setOrigins(List.of("Brazil", "Ethiopia"));
        float[] mockFlavor = new float[0];
        when(flavorVectorService.getUnifiedFlavorVector(any(), any())).thenReturn(mockFlavor);
        // when
        float[] result = coffeeVectorService.createFlavorVector(baseDto);
        // then: Origin part starts after 9 simple attributes + 4 Process length
        // "Brazil" is index 0 in ORIGINS, "Ethiopia" is index 2
        int originStart = NORM_PART_SIZE + PROCESS_PART_SIZE;
        assertEquals(1.0f, result[originStart + 0], "Brazil should be 1.0");
        assertEquals(1.0f, result[originStart + 2], "Ethiopia should be 1.0");
        assertEquals(0.0f, result[originStart + 1], "Colombia should be 0.0");
    }

    @Test
    void shouldHandleOutOfBoundsValuesByClamping() {
        // given
        baseDto.setScaScore(110); // Above max 100
        baseDto.setAcidity(11); // Above max 10
        when(flavorVectorService.getUnifiedFlavorVector(any(), any())).thenReturn(new float[0]);
        // when
        float[] result = coffeeVectorService.createFlavorVector(baseDto);
        // then
        assertEquals(1.0f, result[2], "SCA Score should be clamped to 1.0");
        assertEquals(1.0f, result[3], "Acidity should be clamped to 1.0");
    }

    @Test
    void shouldCombineAllVectorPartsInCorrectOrder() {
        // given
        float[] mockFlavorPart = new float[FlavorCategory.values().length];
        mockFlavorPart[6] = 1.0f;
        when(flavorVectorService.getUnifiedFlavorVector(any(), any())).thenReturn(mockFlavorPart);
        // when
        float[] result = coffeeVectorService.createFlavorVector(baseDto);
        // then: Length: 9 (simple) + 4 (process) + 10 (origins) + 7 (flavor) = 30
        assertEquals(NORM_PART_SIZE + PROCESS_PART_SIZE + ORIGIN_PART_SIZE + FLAVOR_PART_SIZE, result.length);
        // Assert Flavor Part is at the end
        assertEquals(0.0f, result[28]);
        assertEquals(1.0f, result[29]);
    }

    @Test
    void shouldHandleNullOriginsGracefully() {
        // Arrange
        baseDto.setOrigins(null);
        when(flavorVectorService.getUnifiedFlavorVector(any(), any())).thenReturn(new float[0]);

        // Act & Assert
        assertDoesNotThrow(() -> {
            float[] result = coffeeVectorService.createFlavorVector(baseDto);
            // Verify origin section is all zeros (Indices 13 through 23)
            int originStart = NORM_PART_SIZE + PROCESS_PART_SIZE;
            for (int i = originStart; i < originStart + ORIGIN_PART_SIZE; i++) {
                assertEquals(0.0f, result[i]);
            }
        });
    }

    @Test
    void createBaseVector_ShouldBeConsistent() {
        // given
        float[] firstCall = coffeeVectorService.createBaseVector();
        float[] secondCall = coffeeVectorService.createBaseVector();
        // then
        assertTrue(firstCall.length > 0);
        assertArrayEquals(firstCall, secondCall, "Base vector should be deterministic");
    }
}
