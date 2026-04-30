package org.recsys.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.recsys.dto.coffee.CoffeeVectorizationDto;

import io.hypersistence.utils.hibernate.type.range.Range;

@ExtendWith(MockitoExtension.class)
class CoffeeVectorServiceTest {

    @Mock
    private FlavorVectorService flavorVectorService;

    @Mock
    private WeightVectorService weightVectorService;

    @InjectMocks
    private CoffeeVectorService coffeeVectorService;

    private CoffeeVectorizationDto baseDto;

    // Based on the Service Implementation:
    // Simple(9) + Process(4: Natural, Washed, Honey, Anaerobic) + Origins(10) +
    // Flavor(7)
    private static final int NORM_PART_SIZE = 9;
    private static final int PROCESS_PART_SIZE = 4;
    private static final int ORIGIN_PART_SIZE = 10;
    private static final int FLAVOR_PART_SIZE = 7;
    private static final int TOTAL_SIZE = NORM_PART_SIZE + PROCESS_PART_SIZE + ORIGIN_PART_SIZE + FLAVOR_PART_SIZE;

    @BeforeEach
    void setUp() {
        baseDto = CoffeeVectorizationDto.builder()
                .origins(List.of("Ethiopia"))
                .process("Washed")
                .roastLevel(2) // MEDIUM
                .altitude(Range.closed(1000, 2500))
                .scaScore(90.0)
                .acidity(5)
                .body(5)
                .aftertaste(5)
                .sweetness(5)
                .bitterness(5)
                .flavorNotes(List.of("Jasmine"))
                .description("Floral")
                .build();
        // mock behavior for weight service to allow inspection of the combined vector
        lenient().when(weightVectorService.getBaseWeightVector()).thenReturn(new float[TOTAL_SIZE]);
        lenient().when(weightVectorService.l2Normalize(any())).thenAnswer(i -> i.getArguments()[0]);
    }

    @Test
    void shouldNormalizeSimpleAttributesCorrectlyUsingMinMaxRange() {
        // given
        float[] mockFlavor = new float[FLAVOR_PART_SIZE];
        when(flavorVectorService.getUnifiedFlavorVector(any(), any())).thenReturn(mockFlavor);
        // when
        float[] result = coffeeVectorService.createFlavorVector(baseDto);
        // then
        // Roast: RoastLevel.MEDIUM (2) on scale 0-4.
        // -1 + ((2-0)*2 / 4) = -1 + 1 = 0.0
        assertEquals(0.0f, result[0], 0.001f);
        // SCA Score: 90 on scale 80-100.
        // 0 + ((90-80)*(1-0) / 20) = 0 + 0.5 = 0.5
        assertEquals(0.5f, result[2], 0.001f);
        // Single Origin boolean: true -> 1.0, false -> -1.0
        assertEquals(1.0f, result[8], 0.001f);
    }

    @Test
    void shouldMultiHotEncodeOriginsCorrectly() {
        // given
        baseDto.setOrigins(List.of("Brazil", "Ethiopia"));
        when(flavorVectorService.getUnifiedFlavorVector(any(), any())).thenReturn(new float[FLAVOR_PART_SIZE]);
        // when
        float[] result = coffeeVectorService.createFlavorVector(baseDto);
        // then: Origin part starts after 9 simple + 4 process
        int originStart = NORM_PART_SIZE + PROCESS_PART_SIZE;
        // ORIGINS = ["Brazil", "Colombia", "Ethiopia" ...]
        assertEquals(1.0f, result[originStart + 0], "Brazil should be 1.0");
        assertEquals(0.0f, result[originStart + 1], "Colombia should be 0.0");
        assertEquals(1.0f, result[originStart + 2], "Ethiopia should be 1.0");
    }

    @Test
    void shouldHandleOutOfBoundsValuesByClamping() {
        // given
        baseDto.setScaScore(110); // Above max 100
        baseDto.setAcidity(-5); // Below min 1
        when(flavorVectorService.getUnifiedFlavorVector(any(), any())).thenReturn(new float[FLAVOR_PART_SIZE]);
        // when
        float[] result = coffeeVectorService.createFlavorVector(baseDto);
        // then
        assertEquals(1.0f, result[2], "SCA Score should be clamped to 1.0");
        assertEquals(-1.0f, result[3], "Acidity should be clamped to -1.0");
    }

    @Test
    void shouldCombineAllVectorPartsInCorrectOrder() {
        // given
        float[] mockFlavorPart = new float[FLAVOR_PART_SIZE];
        mockFlavorPart[FLAVOR_PART_SIZE - 1] = 0.75f; // Set last element
        when(flavorVectorService.getUnifiedFlavorVector(any(), any())).thenReturn(mockFlavorPart);
        // when
        float[] result = coffeeVectorService.createFlavorVector(baseDto);
        // then
        assertEquals(TOTAL_SIZE, result.length);
        assertEquals(0.75f, result[TOTAL_SIZE - 1]);
        verify(weightVectorService).applyFeatureWeights(any(), any());
        verify(weightVectorService).l2Normalize(any());
    }

    @Test
    void shouldHandleNullOriginsGracefully() {
        // given
        baseDto.setOrigins(null);
        when(flavorVectorService.getUnifiedFlavorVector(any(), any())).thenReturn(new float[FLAVOR_PART_SIZE]);
        // when
        float[] result = coffeeVectorService.createFlavorVector(baseDto);
        // then
        int originStart = NORM_PART_SIZE + PROCESS_PART_SIZE;
        for (int i = originStart; i < originStart + ORIGIN_PART_SIZE; i++) {
            assertEquals(0.0f, result[i], "Origin index " + i + " should be 0.0");
        }
    }

    @Test
    void createBaseVector_ShouldBeDeterministicAndCallServices() {
        // given
        when(flavorVectorService.getUnifiedFlavorVector(any(), any())).thenReturn(new float[FLAVOR_PART_SIZE]);
        // when
        float[] firstCall = coffeeVectorService.createBaseVector();
        float[] secondCall = coffeeVectorService.createBaseVector();
        // then
        assertArrayEquals(firstCall, secondCall);
        verify(flavorVectorService, times(2)).getUnifiedFlavorVector(any(), any());
    }
}
