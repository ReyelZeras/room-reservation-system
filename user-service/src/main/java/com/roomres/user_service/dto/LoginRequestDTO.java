package com.roomres.user_service.dto;

import lombok.Data;

@Data
public class LoginRequestDTO {
    private String email;
    private String password;
}