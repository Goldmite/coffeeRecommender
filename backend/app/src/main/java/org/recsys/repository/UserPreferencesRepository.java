package org.recsys.repository;

import org.recsys.model.UserPreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserPreferencesRepository extends JpaRepository<UserPreferences, Long> {

    @Query("""
                SELECT CASE WHEN p.tasteProfile IS NULL THEN true ELSE false END
                FROM UserPreferences p
                WHERE p.userId = :userId
            """)
    boolean isNewUser(Long userId);

}
