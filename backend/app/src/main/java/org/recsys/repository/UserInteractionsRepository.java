package org.recsys.repository;

import org.recsys.model.UserInteractions;
import org.recsys.model.keys.UserInteractionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserInteractionsRepository extends JpaRepository<UserInteractions, UserInteractionId> {

    @Query("SELECT COUNT(ui) FROM UserInteractions ui WHERE ui.userId = :userId")
    int countByUserId(@Param("userId") Long userId);
}
