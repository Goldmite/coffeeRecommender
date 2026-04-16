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

    @Override
    public void run(String... args) {
        for (int i = 1; i <= 100; i++) {
            service.addCoffee(generator.generate(i));
        }
    }
}
