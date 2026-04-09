package org.recsys.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RoastLevel {
    LIGHT("Light"),
    MEDIUM_LIGHT("Medium Light"),
    MEDIUM("Medium"),
    MEDIUM_DARK("Medium Dark"),
    DARK("Dark");

    private final String label;
}
