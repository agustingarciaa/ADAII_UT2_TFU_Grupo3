package com.adaucu.demo.api.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginPeticion(@NotBlank String email, @NotBlank String contrasena) {
}
