package org.recsys.dto.recommendation;

import java.util.ArrayList;
import java.util.List;

import org.recsys.mapper.IndexMapper;
import org.recsys.proto.TrainedModelProto;

import com.google.protobuf.InvalidProtocolBufferException;

public record TrainedModel(
        float[] userFactors,
        float[] coffeeFactors,
        float[] userBiases,
        float[] coffeeBiases,
        float[] userAlphas,
        float[] coffeeBinBiases,
        int K,
        float globalMean,
        IndexMapper userMapper,
        IndexMapper coffeeMapper) {

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

    public byte[] serialize() {
        TrainedModelProto proto = TrainedModelProto.newBuilder()
                .addAllUserFactors(floatArrayToList(userFactors))
                .addAllCoffeeFactors(floatArrayToList(coffeeFactors))
                .addAllUserBiases(floatArrayToList(userBiases))
                .addAllCoffeeBiases(floatArrayToList(coffeeBiases))
                .addAllUserAlphas(floatArrayToList(userAlphas))
                .addAllCoffeeBinBiases(floatArrayToList(coffeeBinBiases))
                .setK(K)
                .setGlobalMean(globalMean)
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
                proto.getK(),
                proto.getGlobalMean(),
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
