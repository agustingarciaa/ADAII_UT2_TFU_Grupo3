package com.adaucu.demo.api.dto;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;

/**
 * asientoId + marcaTiempo: la marca es la que el cliente observo en
 * GET /partidos/{id}/asientos. Viaja de vuelta para que el servidor pueda
 * detectar si el asiento cambio de estado desde esa lectura (tactica de
 * timestamp).
 */
public record ItemAsientoPeticion(

        @NotNull
        Long asientoId,

        @NotNull
        Instant marcaTiempo) {
}
