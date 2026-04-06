package org.recsys.dto.user;

public record AuthResponse(
        String token,
        UserResponse user) {
}
