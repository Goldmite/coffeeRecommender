package org.recsys.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelMetadata {
    private int k;
    private float gamma;
    private float lambda;
    private int epochs;
    private int userCount;
    private int coffeeCount;
}
