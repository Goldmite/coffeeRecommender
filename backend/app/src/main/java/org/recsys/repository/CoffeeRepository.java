package org.recsys.repository;

import org.recsys.model.CoffeeBean;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoffeeRepository extends JpaRepository<CoffeeBean, Long> {

}
