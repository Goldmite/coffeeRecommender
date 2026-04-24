package org.recsys.repository;

import java.util.Optional;

import org.recsys.model.TrainedModelArtifact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface TrainedModelRepository extends JpaRepository<TrainedModelArtifact, Integer> {

    Optional<TrainedModelArtifact> findByIsActiveTrue();

    @Query("SELECT COALESCE(MAX(version), 0) FROM TrainedModelArtifact")
    int findMaxVersion();

    @Modifying
    @Query("UPDATE TrainedModelArtifact SET isActive = false WHERE isActive = true")
    void deactivateOldModels();
}
