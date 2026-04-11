package org.recsys.model;

import io.hypersistence.utils.hibernate.type.array.ListArrayType;
import io.hypersistence.utils.hibernate.type.range.PostgreSQLRangeType;
import io.hypersistence.utils.hibernate.type.range.Range;

import java.util.List;

import org.hibernate.annotations.Type;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "coffee_features")
@Getter
@Setter
@NoArgsConstructor
public class CoffeeFeatures {

    @Id
    @Column(name = "coffee_id")
    private Long id;

    @Column(nullable = false)
    private String origin;
    private String process;

    @Enumerated(EnumType.ORDINAL)
    private RoastLevel roastLevel;

    @Column(name = "description", columnDefinition = "vector(100)")
    private float[] descriptionVector;

    @Type(PostgreSQLRangeType.class)
    @Column(columnDefinition = "int4range")
    private Range<Integer> altitude;

    private double scaScore;
    private Integer acidity;
    private Integer body;
    private Integer aftertaste;
    private Integer sweetness;
    private Integer bitterness;

    @Column(columnDefinition = "text[]")
    private List<String> flavorNotes;

    @Column(columnDefinition = "vector(100)")
    private float[] flavorVector;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "coffee_id")
    private CoffeeBean coffeeBean;
}
