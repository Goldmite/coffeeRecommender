package org.recsys.dto.user;

import org.recsys.model.User;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserResponse {

    private Long id;
    private String name;
    private String email;

    public static UserResponse fromEntity(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }
}
