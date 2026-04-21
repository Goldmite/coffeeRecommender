package org.recsys.model;

import io.hypersistence.utils.hibernate.type.range.PostgreSQLRangeType;
import io.hypersistence.utils.hibernate.type.range.Range;

import java.util.List;

import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

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

    @Column(columnDefinition = "text[]")
    private List<String> origins;

    @Enumerated(EnumType.STRING)
    private Processing process;

    @Enumerated(EnumType.ORDINAL)
    private RoastLevel roastLevel;

    @Column(name = "coffee_description")
    private String description;

    @Type(PostgreSQLRangeType.class)
    @Column(columnDefinition = "int4range")
    private Range<Integer> altitude;

    private Double scaScore;
    private Integer acidity;
    private Integer body;
    private Integer aftertaste;
    private Integer sweetness;
    private Integer bitterness;

    @Column(columnDefinition = "text[]")
    private List<String> flavorNotes;

    /**
     * Index-value mapping
     * SIMPLE NORM ATTRIBUTES:
     * 0 - roast level,
     * 1 - altitude,
     * 2 - sca score,
     * 3 - acidity,
     * 4 - body,
     * 5 - aftertaste,
     * 6 - sweetness,
     * 7 - bitterness,
     * 8 - is single origin;
     * PROCESSING:
     * 9 - is "Natural",
     * 10 - is "Washed",
     * 11 - is "Honey",
     * 12 - is "Other";
     * ORIGINS:
     * 13 - is "Brazil",
     * 14 - is "Colombia",
     * 15 - is "Ethiopia",
     * 16 - is "Peru",
     * 17 - is "Kenya",
     * 18 - is "Nicaragua",
     * 19 - is "Guatemala",
     * 20 - is "Indonesia",
     * 21 - is "India",
     * 22 - is "Other" origin;
     * FLAVOR CATEGORIES:
     * 23 - is "Fruity",
     * 24 - is "Floral",
     * 25 - is "Sweet",
     * 26 - is "NuttyCocoa",
     * 27 - is "Spices",
     * 28 - is "Vegetal",
     * 29 - is "Sour";
     */
    @Column(columnDefinition = "vector(30)")
    @JdbcTypeCode(SqlTypes.VECTOR)
    @Array(length = 3)
    private float[] flavorVector;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "coffee_id")
    private CoffeeBean coffeeBean;
}
