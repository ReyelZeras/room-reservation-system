package com.roomres.user_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangePasswordRequestDTO {

    @NotBlank(message = "A senha atual é obrigatória.")
    private String currentPassword;

    @NotBlank(message = "A nova senha é obrigatória.")
    private String newPassword;
}