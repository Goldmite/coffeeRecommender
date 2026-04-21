package org.recsys.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FlavorCategory {
    FRUITY("Fruity"),
    FLORAL("Floral"),
    SWEET("Sweet"),
    NUTTYCOCOA("NuttyCocoa"),
    SPICES("Spices"),
    VEGETAL("Vegetal"),
    SOUR("Sour");

    private final String category;
}
