package org.recsys.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.recsys.dto.coffee.CoffeeBeanRequest;
import org.recsys.dto.coffee.CoffeeBeanResponse;
import org.recsys.model.CoffeeBean;
import org.recsys.model.CoffeeFeatures;

import io.hypersistence.utils.hibernate.type.range.Range;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CoffeeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "features.coffeeBean", ignore = true)
    CoffeeBean toEntity(CoffeeBeanRequest dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "coffeeBean", ignore = true)
    CoffeeFeatures toFeaturesEntity(CoffeeBeanRequest.FeaturesRequest dto);

    @Mapping(target = "origins", source = "features.origins")
    @Mapping(target = "process", source = "features.process")
    @Mapping(target = "roastLevel", source = "features.roastLevel")
    // @Mapping(target = "description", source = "features.description")
    @Mapping(target = "altitude", source = "features.altitude")
    @Mapping(target = "scaScore", source = "features.scaScore")
    @Mapping(target = "acidity", source = "features.acidity")
    @Mapping(target = "body", source = "features.body")
    @Mapping(target = "aftertaste", source = "features.aftertaste")
    @Mapping(target = "sweetness", source = "features.sweetness")
    @Mapping(target = "bitterness", source = "features.bitterness")
    @Mapping(target = "flavorNotes", source = "features.flavorNotes")
    CoffeeBeanResponse toResponse(CoffeeBean bean);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromDto(CoffeeBeanRequest dto, @MappingTarget CoffeeBean entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "coffeeBean", ignore = true)
    void updateFeaturesFromDto(CoffeeBeanRequest.FeaturesRequest dto, @MappingTarget CoffeeFeatures subEntity);

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
