package org.recsys.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Processing {
    NATURAL("Natural"),
    WASHED("Washed"),
    HONEY("Honey"),
    OTHER("Other");

    private final String process;
}
