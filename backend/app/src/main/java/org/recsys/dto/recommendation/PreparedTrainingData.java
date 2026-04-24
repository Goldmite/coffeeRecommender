package org.recsys.dto.recommendation;

import java.util.List;

import org.recsys.mapper.IndexMapper;

public record PreparedTrainingData(
                List<RatingTriplet> triplets,
                IndexMapper userMapper,
                IndexMapper coffeeMapper,
                float globalMean,
                long minTimestamp) {
}
