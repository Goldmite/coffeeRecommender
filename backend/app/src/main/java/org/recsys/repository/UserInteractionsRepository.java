package org.recsys.repository;

import org.recsys.model.UserInteractions;
import org.recsys.model.keys.UserInteractionId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserInteractionsRepository extends JpaRepository<UserInteractions, UserInteractionId> {

}
