package org.recsys.repository;

import org.recsys.model.CoffeeFeatures;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoffeeFeatureRepository extends JpaRepository<CoffeeFeatures, Long> {

}
