package org.recsys.repository;

import java.util.List;

import org.recsys.dto.recommendation.SimilarCoffees;
import org.recsys.model.CoffeeBean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CoffeeRepository extends JpaRepository<CoffeeBean, Long> {

        @Query("SELECT f.description FROM CoffeeBean c JOIN c.features f")
        List<String> findAllDescriptions();

        @Query("SELECT id FROM CoffeeBean")
        List<Long> findAllIds();

        @Query(value = "SELECT * FROM coffee_beans c " +
                        "JOIN coffee_features f ON c.id = f.coffee_id " +
                        "ORDER BY f.flavor_vector <=> cast(:vector as vector) " +
                        "LIMIT :n", nativeQuery = true)
        List<CoffeeBean> findTopNSimilarBeansByVector(@Param("vector") float[] vector, @Param("n") int n);

        @Query(value = """
                        SELECT c.id, (f.flavor_vector <=> cast(:vector as vector)) as distance
                        FROM coffee_beans c
                        JOIN coffee_features f ON c.id = f.coffee_id
                        ORDER BY distance
                        LIMIT :n
                        """, nativeQuery = true)
        List<SimilarCoffees> findTopSimilarCoffeeCandidates(@Param("vector") float[] vector, @Param("n") int n);
}
