package org.recsys.model;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "trained_models")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainedModelArtifact extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer version;

    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "model_data", nullable = false)
    private byte[] data;
    private double rmse;
    private Boolean isActive;

    @JdbcTypeCode(SqlTypes.JSON)
    private ModelMetadata metadata;
}
