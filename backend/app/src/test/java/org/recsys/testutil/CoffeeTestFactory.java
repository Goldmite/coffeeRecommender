package org.recsys.testutil;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import org.recsys.data.CoffeeDataGenerator;
import org.recsys.dto.coffee.CoffeeBeanRequest;

public class CoffeeTestFactory {

    private static final CoffeeDataGenerator generator = new CoffeeDataGenerator(new Random(1));

    public static CoffeeBeanRequest createRequest(int index) {
        return generator.generate(index);
    }

    public static List<CoffeeBeanRequest> createBatchOfRequests(int count) {
        return IntStream.rangeClosed(1, count).mapToObj(generator::generate).toList();
    }
}
