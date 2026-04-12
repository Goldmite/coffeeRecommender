package org.recsys.repository;

import java.util.List;

import org.recsys.model.Shop;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopRepository extends JpaRepository<Shop, Integer> {

    List<Shop> findAllByIsActive(Boolean isActive);
}
