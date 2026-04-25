package org.recsys.dto.recommendation;

import java.util.ArrayList;
import java.util.List;

import org.recsys.mapper.IndexMapper;
import org.recsys.proto.TrainedModelProto;
import org.recsys.util.PredictionUtils;

import com.google.protobuf.InvalidProtocolBufferException;

public record TrainedModel(
        float[] userFactors,
        float[] coffeeFactors,
        float[] userBiases,
        float[] coffeeBiases,
        float[] userAlphas,
        float[] coffeeBinBiases,
        float[] userTimestampMeans,
        int K,
        float globalMean,
        long minTimestamp,
        IndexMapper userMapper,
        IndexMapper coffeeMapper) {

    public float predict(Long userId, Long coffeeId, long targetTimestamp) {
        Integer u = userMapper.getInternalIndex(userId);
        Integer i = coffeeMapper.getInternalIndex(coffeeId);
        // fallback
        if (u == null || i == null)
            return globalMean;

        int uOffset = u * K;
        int iOffset = i * K;
        int binIdx = PredictionUtils.calculateBinIndex(targetTimestamp, minTimestamp);
        int bOffset = PredictionUtils.getCoffeeBinOffset(i, binIdx);

        float dev = PredictionUtils.calculateUserDev(targetTimestamp, userTimestampMeans[u], 0.4);

        float dotProduct = 0;
        for (int k = 0; k < K; k++) {
            dotProduct += userFactors[uOffset + k] * coffeeFactors[iOffset + k];
        }
        // PREDICTION (Score)
        return PredictionUtils.calculatePrediction(globalMean, userBiases[u], userAlphas[u], dev, coffeeBiases[i],
                coffeeBinBiases[bOffset], dotProduct);
    }

    public byte[] serialize() {
        TrainedModelProto proto = TrainedModelProto.newBuilder()
                .addAllUserFactors(floatArrayToList(userFactors))
                .addAllCoffeeFactors(floatArrayToList(coffeeFactors))
                .addAllUserBiases(floatArrayToList(userBiases))
                .addAllCoffeeBiases(floatArrayToList(coffeeBiases))
                .addAllUserAlphas(floatArrayToList(userAlphas))
                .addAllCoffeeBinBiases(floatArrayToList(coffeeBinBiases))
                .addAllUserTimestampMeans(floatArrayToList(userTimestampMeans))
                .setK(K)
                .setGlobalMean(globalMean)
                .setMinTimestamp(minTimestamp)
                .putAllUserIdToIdx(userMapper.getInternalMap())
                .putAllCoffeeIdToIdx(coffeeMapper.getInternalMap())
                .build();
        return proto.toByteArray();
    }

    public static TrainedModel deserialize(byte[] bytes) throws InvalidProtocolBufferException {
        TrainedModelProto proto = TrainedModelProto.parseFrom(bytes);

        return new TrainedModel(
                listToFloatArray(proto.getUserFactorsList()),
                listToFloatArray(proto.getCoffeeFactorsList()),
                listToFloatArray(proto.getUserBiasesList()),
                listToFloatArray(proto.getCoffeeBiasesList()),
                listToFloatArray(proto.getUserAlphasList()),
                listToFloatArray(proto.getCoffeeBinBiasesList()),
                listToFloatArray(proto.getUserTimestampMeansList()),
                proto.getK(),
                proto.getGlobalMean(),
                proto.getMinTimestamp(),
                new IndexMapper(proto.getUserIdToIdxMap()),
                new IndexMapper(proto.getCoffeeIdToIdxMap()));
    }

    // Helpers
    private static List<Float> floatArrayToList(float[] array) {
        List<Float> list = new ArrayList<>(array.length);
        for (float f : array)
            list.add(f);
        return list;
    }

    private static float[] listToFloatArray(List<Float> list) {
        float[] array = new float[list.size()];
        for (int i = 0; i < list.size(); i++)
            array[i] = list.get(i);
        return array;
    }
}
