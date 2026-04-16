package org.recsys.demo;

import org.recsys.data.CoffeeDataGenerator;
import org.recsys.service.CoffeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("demo")
public class DemoSeeder implements CommandLineRunner {

    @Autowired
    private CoffeeDataGenerator generator;
    @Autowired
    private CoffeeService service;
    // @Autowired
    // private ShopService shopService;

    @Override
    public void run(String... args) {
        /*
         * System.out.println("Seeding 5 coffee shops...");
         * List<ShopRequest> shops = List.of(
         * new ShopRequest("Shop A", "https://example.com", null),
         * new ShopRequest("Shop B", "https://example.com", null),
         * new ShopRequest("Shop C", "https://example.com", null),
         * new ShopRequest("Shop D", "https://example.com", null),
         * new ShopRequest("Shop E", "https://example.com", null));
         * shopService.createShops(shops);
         */
        System.out.println("Seeding 100 coffees...");
        for (int i = 1; i <= 100; i++) {
            service.addCoffee(generator.generate(i));
        }
    }
}
