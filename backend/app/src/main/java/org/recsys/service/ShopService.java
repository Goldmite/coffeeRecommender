package org.recsys.service;

import java.util.List;

import org.recsys.dto.shop.ShopRequest;
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
}
