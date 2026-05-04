package org.recsys.repository;

import org.recsys.model.UserInteractions;
import org.recsys.model.keys.UserInteractionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.transaction.Transactional;

public interface UserInteractionsRepository extends JpaRepository<UserInteractions, UserInteractionId> {

    @Query("SELECT COUNT(ui) FROM UserInteractions ui WHERE ui.userId = :userId")
    int countByUserId(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserInteractions ui WHERE ui.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
