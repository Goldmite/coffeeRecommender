package org.recsys.dto.user;

import lombok.Value;

@Value
public class UserResponse {

    private Long id;
    private String name;
    private String email;
}
