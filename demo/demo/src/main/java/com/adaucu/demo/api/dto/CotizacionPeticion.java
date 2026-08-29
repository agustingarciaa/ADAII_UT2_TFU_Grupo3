package com.adaucu.demo.api.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CotizacionPeticion(

        @NotNull
        Long partidoId,

        @NotEmpty
        List<Long> asientoIds,

        String politica) {
}
