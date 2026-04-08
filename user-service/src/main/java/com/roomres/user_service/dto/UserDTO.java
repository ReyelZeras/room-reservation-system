package com.roomres.user_service.dto;

import com.roomres.user_service.model.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class UserDTO {
    private UUID id;
    private String username;
    private String email;
    private String name;
    private String role;

    public UserDTO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.name = user.getName();
        this.role = user.getRole();
    }
}