package com.adaucu.demo.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistroUsuarioPeticion(

        @NotBlank @Email
        String email,

        @NotBlank @Size(min = 8, message = "La contrasena debe tener al menos 8 caracteres")
        String contrasena,

        @NotBlank
        String nombre) {
}
