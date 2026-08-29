package com.adaucu.demo.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ReservaPeticion(

        @NotNull
        Long partidoId,

        @NotEmpty @Valid
        List<ItemAsientoPeticion> asientos,

        String politica) {
}
