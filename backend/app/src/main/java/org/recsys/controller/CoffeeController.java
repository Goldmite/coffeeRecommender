package org.recsys.controller;

import java.util.List;

import org.recsys.dto.coffee.CoffeeBeanRequest;
import org.recsys.dto.coffee.CoffeeBeanResponse;
import org.recsys.dto.coffee.PurchasedCoffeeDto;
import org.recsys.mapper.CoffeeMapper;
import org.recsys.service.CoffeeService;
import org.recsys.service.UserInteractionsService;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/coffee")
@RequiredArgsConstructor
public class CoffeeController {

    private final CoffeeService coffeeService;
    private final CoffeeMapper mapper;
    private final UserInteractionsService interactionsService;

    @GetMapping(params = "id")
    public ResponseEntity<CoffeeBeanResponse> getCoffeeById(@RequestParam Long id) throws NotFoundException {
        return ResponseEntity.ok(mapper.toResponse(coffeeService.getCoffeeById(id)));
    }

    @GetMapping
    public ResponseEntity<List<CoffeeBeanResponse>> getAllCoffees() {
        return ResponseEntity.ok(coffeeService.getAllCoffees().stream().map(bean -> mapper.toResponse(bean)).toList());
    }

    @GetMapping("/purchased/user/{userId}")
    public ResponseEntity<Page<PurchasedCoffeeDto>> getPurchasedCoffeesByUser(@PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(interactionsService.findPurchasedCoffeesByUser(userId, page, size));
    }

    @PostMapping
    public ResponseEntity<CoffeeBeanResponse> addNewCoffee(@Valid @RequestBody CoffeeBeanRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toResponse(coffeeService.addCoffee(req)));
    }

    @PutMapping
    public ResponseEntity<CoffeeBeanResponse> updateCoffee(@RequestParam Long id,
            @Valid @RequestBody CoffeeBeanRequest req) throws NotFoundException {
        return ResponseEntity.ok(mapper.toResponse(coffeeService.updateCoffee(id, req)));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteCoffeeById(@RequestParam Long id) {
        coffeeService.deleteCoffeeById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/vector")
    public ResponseEntity<List<CoffeeBeanResponse>> batchUpdateCoffeeVectors() {
        return ResponseEntity
                .ok(coffeeService.batchUpdateCoffeeVectors().stream().map(bean -> mapper.toResponse(bean)).toList());
    }
}
