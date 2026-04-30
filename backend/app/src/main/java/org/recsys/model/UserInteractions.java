package org.recsys.model;

import java.time.Instant;

import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.recsys.model.keys.UserInteractionId;

import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "user_interactions")
@IdClass(UserInteractionId.class)
@Getter
@Setter
@ToString(exclude = { "user", "coffeeBean" })
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@DynamicUpdate
public class UserInteractions extends AuditEntity {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "coffee_id")
    private Long coffeeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    // keep interaction even if coffee is deleted
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coffee_id", insertable = false, updatable = false, foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
    private CoffeeBean coffeeBean;

    @Column(name = "rating")
    private Integer rating;

    private Instant purchaseDate;

    @Builder.Default
    @Column(name = "is_clicked")
    private Boolean isClicked = false;

    @Builder.Default
    @Column(name = "is_purchased")
    private Boolean isPurchased = false;
}
