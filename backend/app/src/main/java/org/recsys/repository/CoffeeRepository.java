package org.recsys.repository;

import java.util.List;

import org.recsys.model.CoffeeBean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CoffeeRepository extends JpaRepository<CoffeeBean, Long> {

    @Query("SELECT f.description FROM CoffeeBean c JOIN c.features f")
    List<String> findAllDescriptions();
}
