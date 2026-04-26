package org.recsys.mapper;

import java.util.Collections;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.recsys.dto.coffee.CoffeeBeanRequest;
import org.recsys.dto.coffee.CoffeeBeanResponse;
import org.recsys.dto.coffee.CoffeeVectorizationDto;
import org.recsys.dto.shop.ShopResponse;
import org.recsys.model.CoffeeBean;
import org.recsys.model.CoffeeFeatures;
import org.recsys.model.Shop;

import io.hypersistence.utils.hibernate.type.range.Range;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CoffeeMapper {

    // Define the valid origins list
    List<String> KNOWN_ORIGINS = List.of(
            "Brazil", "Colombia", "Ethiopia", "Peru", "Kenya",
            "Nicaragua", "Guatemala", "Indonesia", "India");

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "shop", ignore = true) // handle in service
    @Mapping(target = "features.id", ignore = true)
    @Mapping(target = "features.coffeeBean", ignore = true)
    @Mapping(target = "features.flavorVector", ignore = true)
    CoffeeBean toEntity(CoffeeBeanRequest dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "coffeeBean", ignore = true)
    @Mapping(target = "flavorVector", ignore = true)
    CoffeeFeatures toFeaturesEntity(CoffeeBeanRequest.FeaturesRequest dto);

    @Mapping(target = "shop", source = "shop")
    @Mapping(target = "origins", source = "features.origins")
    @Mapping(target = "process", source = "features.process")
    @Mapping(target = "roastLevel", source = "features.roastLevel")
    @Mapping(target = "description", source = "features.description")
    @Mapping(target = "altitude", source = "features.altitude")
    @Mapping(target = "scaScore", source = "features.scaScore")
    @Mapping(target = "acidity", source = "features.acidity")
    @Mapping(target = "body", source = "features.body")
    @Mapping(target = "aftertaste", source = "features.aftertaste")
    @Mapping(target = "sweetness", source = "features.sweetness")
    @Mapping(target = "bitterness", source = "features.bitterness")
    @Mapping(target = "flavorNotes", source = "features.flavorNotes")
    CoffeeBeanResponse toResponse(CoffeeBean bean);

    ShopResponse mapShop(Shop shop);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "shop", ignore = true)
    @Mapping(target = "features.id", ignore = true)
    @Mapping(target = "features.coffeeBean", ignore = true)
    @Mapping(target = "features.flavorVector", ignore = true)
    void updateEntityFromDto(CoffeeBeanRequest dto, @MappingTarget CoffeeBean entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "coffeeBean", ignore = true)
    @Mapping(target = "flavorVector", ignore = true)
    void updateFeaturesFromDto(CoffeeBeanRequest.FeaturesRequest dto, @MappingTarget CoffeeFeatures subEntity);

    @Mapping(target = "coffeeId", source = "id")
    @Mapping(target = "origins", source = "features.origins", qualifiedByName = "mapOrigins")
    @Mapping(target = "process", source = "features.process")
    @Mapping(target = "roastLevel", source = "features.roastLevel")
    @Mapping(target = "description", source = "features.description")
    @Mapping(target = "altitude", source = "features.altitude")
    @Mapping(target = "scaScore", source = "features.scaScore", defaultValue = "80")
    @Mapping(target = "acidity", source = "features.acidity", defaultValue = "5")
    @Mapping(target = "body", source = "features.body", defaultValue = "5")
    @Mapping(target = "aftertaste", source = "features.aftertaste", defaultValue = "5")
    @Mapping(target = "sweetness", source = "features.sweetness", defaultValue = "5")
    @Mapping(target = "bitterness", source = "features.bitterness", defaultValue = "5")
    @Mapping(target = "flavorNotes", source = "features.flavorNotes")
    CoffeeVectorizationDto toVectorDto(CoffeeBean bean);

    /**
     * Maps the origins list. If an origin is not in the KNOWN_ORIGINS list,
     * it maps it as "Other".
     * 
     * @param origins
     * @return
     */
    @Named("mapOrigins")
    default List<String> mapOrigins(List<String> origins) {
        if (origins == null)
            return Collections.emptyList();

        return origins.stream()
                .map(origin -> KNOWN_ORIGINS.stream()
                        .anyMatch(known -> known.equalsIgnoreCase(origin))
                                ? origin
                                : "Other")
                .distinct()
                .toList();
    }

    /**
     * Custom mapping for List<Integer> to Range<Integer>
     * 
     * @param list
     * @return
     */
    default Range<Integer> mapListToRange(List<Integer> list) {
        if (list == null)
            return null;

        return Range.closed(list.getFirst(), list.getLast());
    }

    /**
     * Custom reverse mapping from Range<Integer> to List<Integer>
     * 
     * @param range
     * @return
     */
    default List<Integer> mapRangeToList(Range<Integer> range) {
        if (range == null || range.isEmpty())
            return null;

        return List.of(range.lower(), range.upper());
    }
}
