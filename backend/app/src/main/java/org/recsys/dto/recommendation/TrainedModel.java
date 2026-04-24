package org.recsys.dto.recommendation;

import java.io.Serializable;

import org.recsys.mapper.IndexMapper;

public record TrainedModel(
        float[] userFactors,
        float[] coffeeFactors,
        float[] userBiases,
        float[] coffeeBiases,
        int K,
        float globalMean,
        IndexMapper userMapper,
        IndexMapper coffeeMapper) implements Serializable {

    public float predict(Long userId, Long coffeeId) {
        Integer u = userMapper.getInternalIndex(userId);
        Integer i = coffeeMapper.getInternalIndex(coffeeId);
        // fallback
        if (u == null || i == null)
            return globalMean;

        int uOffset = u * K;
        int iOffset = i * K;

        float dotProduct = 0;
        for (int k = 0; k < K; k++) {
            dotProduct += userFactors[uOffset + k] * coffeeFactors[iOffset + k];
        }
        return globalMean + userBiases[u] + coffeeFactors[i] + dotProduct;
    }
}
