package org.recsys.controller;

import java.util.List;

import org.recsys.dto.shop.ShopResponse;
import org.recsys.service.ShopService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;

    @GetMapping
    public ResponseEntity<List<ShopResponse>> getShops(@RequestParam(name = "active") Boolean isActive) {
        return ResponseEntity.ok(shopService.getAllByActivity(isActive).stream()
                .map(shop -> new ShopResponse(shop.getId(), shop.getName(), shop.getShopUrl(), shop.getIsActive()))
                .toList());
    }

    @PostMapping
    public ResponseEntity addShops() {

    }

    @PutMapping
    public ResponseEntity updateShopInfo() {

    }

    @DeleteMapping
    public ResponseEntity removeShops() {

    }

}
