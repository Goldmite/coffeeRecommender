package org.recsys.service;

import java.util.List;

import org.recsys.model.Shop;
import org.recsys.repository.ShopRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final ShopRepository shopRepository;

    public List<Shop> getAllByActivity(Boolean isActive) {
        return shopRepository.findAllByIsActive(isActive);
    }
}
