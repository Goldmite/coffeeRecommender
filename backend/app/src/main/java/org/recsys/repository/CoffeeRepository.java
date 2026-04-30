package org.recsys.repository;

import java.util.List;

import org.recsys.dto.recommendation.SimilarCoffees;
import org.recsys.model.CoffeeBean;
import org.recsys.model.UserInteractions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CoffeeRepository extends JpaRepository<CoffeeBean, Long> {

        @Override
        @Query("SELECT c FROM CoffeeBean c JOIN FETCH c.shop WHERE c.id IN :ids")
        List<CoffeeBean> findAllById(@Param("ids") Iterable<Long> ids);

        @Query("SELECT f.description FROM CoffeeBean c JOIN c.features f")
        List<String> findAllDescriptions();

        @Query("SELECT c.id FROM CoffeeBean c WHERE (:shopIds IS NULL OR c.shop.id IN :shopIds)")
        List<Long> findAllIdsInShops(@Param("shopIds") Iterable<Integer> shopIds);

        @Query(value = """
                        SELECT c.id, 1 - (f.flavor_vector <=> cast(:vector as vector)) as similarity
                        FROM coffee_beans c
                        JOIN coffee_features f ON c.id = f.coffee_id
                        WHERE f.flavor_vector IS NOT NULL
                        AND (:shopIds IS NULL OR c.shop_id IN :shopIds)
                        ORDER BY similarity DESC
                        LIMIT :n
                        """, nativeQuery = true)
        List<SimilarCoffees> findTopSimilarCoffeeCandidates(@Param("vector") float[] vector, @Param("n") int n,
                        @Param("shopIds") Iterable<Integer> shopIds);

        @Query("SELECT i FROM UserInteractions i JOIN FETCH i.coffeeBean WHERE i.userId = :userId AND i.isPurchased = true ORDER BY i.purchaseDate DESC")
        Page<UserInteractions> findPurchasedCoffeesByUserId(@Param("userId") Long userId, Pageable pageable);
}
