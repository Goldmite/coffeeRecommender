package org.recsys.controller;

import java.util.List;

import org.recsys.dto.shop.ShopRequest;
import org.recsys.dto.shop.ShopResponse;
import org.recsys.dto.shop.ShopUpdateRequest;
import org.recsys.service.ShopService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    public ResponseEntity<List<ShopResponse>> getShops(
            @RequestParam(name = "active", defaultValue = "true") Boolean isActive) {
        return ResponseEntity
                .ok(shopService.getAllByActivity(isActive).stream().map(ShopResponse::fromEntity).toList());
    }

    @PostMapping
    public ResponseEntity<List<ShopResponse>> addShops(@RequestBody List<ShopRequest> req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(shopService.createShops(req).stream().map(ShopResponse::fromEntity).toList());
    }

    @PutMapping
    public ResponseEntity<List<ShopResponse>> updateShopInfo(@RequestBody List<ShopUpdateRequest> req) {
        return ResponseEntity.ok(shopService.updateShops(req).stream().map(ShopResponse::fromEntity).toList());
    }

    @DeleteMapping
    public ResponseEntity<Void> removeShops() {

    }

}
