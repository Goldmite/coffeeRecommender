package org.recsys.model.keys;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInteractionId implements Serializable {
    private Long userId;
    private Long coffeeId;
}