package org.recsys.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.recsys.dto.shop.ShopRequest;
import org.recsys.dto.shop.ShopUpdateRequest;
import org.recsys.model.Shop;
import org.recsys.repository.ShopRepository;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final ShopRepository shopRepository;

    public List<Shop> getAllByActivity(Boolean isActive) {
        return shopRepository.findAllByIsActive(isActive);
    }

    @Transactional
    public List<Shop> createShops(List<ShopRequest> requestedShops) {
        List<Shop> shopsToAdd = requestedShops.stream()
                .map(ShopRequest::toEntity).toList();
        return shopRepository.saveAll(shopsToAdd);
    }

    @Transactional
    public List<Shop> updateShops(List<ShopUpdateRequest> updatedShops) {
        List<Integer> ids = updatedShops.stream().map(ShopUpdateRequest::getId).toList();
        List<Shop> shops = shopRepository.findAllById(ids);

        Map<Integer, ShopUpdateRequest> updateMap = updatedShops.stream()
                .collect(Collectors.toMap(ShopUpdateRequest::getId, u -> u));

        for (Shop shop : shops) {
            ShopUpdateRequest update = updateMap.get(shop.getId());
            if (update != null) {
                if (update.getName() != null)
                    shop.setName(update.getName());
                if (update.getShopUrl() != null)
                    shop.setShopUrl(update.getShopUrl());
                if (update.getIsActive() != null)
                    shop.setIsActive(update.getIsActive());
            }
        }
        return shopRepository.saveAll(shops);
    }

    @Transactional
    public void deleteShops(List<Integer> ids) {
        shopRepository.deleteAllById(ids);
    }
}
