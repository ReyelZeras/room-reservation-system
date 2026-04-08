package com.roomres.user_service.dto;

import java.util.UUID;

public record UserResponseDTO (
        UUID id,
        String username,
        String email,
        String name,
        String role
){
}
