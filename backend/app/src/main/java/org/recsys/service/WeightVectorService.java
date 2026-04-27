package org.recsys.service;

import java.util.Arrays;

import org.recsys.config.FeatureWeightConfig;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WeightVectorService {

    private final FeatureWeightConfig config;

    public float[] getBaseWeightVector() {
        float[] weights = new float[30];
        // Fill with neutral weights frist
        Arrays.fill(weights, 1.0f);
        // Map configurations
        weights[0] = config.getRoastLevel();
        weights[1] = config.getAltitude();
        weights[2] = config.getScaScore();
        weights[3] = config.getAcidity();
        weights[4] = config.getBody();
        weights[5] = config.getAftertaste();
        weights[6] = config.getSweetness();
        weights[7] = config.getBitterness();
        weights[8] = config.getSingleOrigin();
        // Mapping flavor categories
        weights[23] = config.getFruity();
        weights[24] = config.getFloral();
        weights[25] = config.getSweet();
        weights[26] = config.getNuttyCocoa();
        weights[27] = config.getSpices();
        weights[28] = config.getVegetal();
        weights[29] = config.getSour();

        // Defaults for the rest

        return weights;
    }
}
