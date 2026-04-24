package org.recsys.demo;

import java.util.List;
import java.util.Random;

import org.recsys.data.CoffeeDataGenerator;
import org.recsys.dto.shop.ShopRequest;
import org.recsys.service.CoffeeService;
import org.recsys.service.ShopService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@Profile({ "demo", "test" })
@RequiredArgsConstructor
public class DemoSeeder implements CommandLineRunner {

    private final CoffeeService service;

    private final Random random;

    private final ShopService shopService;

    @Override
    public void run(String... args) {

        System.out.println("Seeding 5 coffee shops...");
        List<ShopRequest> shops = List.of(
                new ShopRequest("Shop A", "https://example.com", null),
                new ShopRequest("Shop B", "https://example.com", null),
                new ShopRequest("Shop C", "https://example.com", null),
                new ShopRequest("Shop D", "https://example.com", null),
                new ShopRequest("Shop E", "https://example.com", null));
        shopService.createShops(shops);

        CoffeeDataGenerator gen = new CoffeeDataGenerator(random);
        System.out.println("Seeding 100 coffees...");
        for (int i = 1; i <= 100; i++) {
            service.addCoffee(gen.generate(i));
        }
    }
}
