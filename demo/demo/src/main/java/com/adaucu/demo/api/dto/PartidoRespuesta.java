package com.adaucu.demo.api.dto;

import com.adaucu.demo.dominio.Partido;
import java.time.Instant;

public record PartidoRespuesta(
        Long id,
        String equipoLocal,
        String equipoVisitante,
        Instant comienzaEn,
        String estado,
        Long estadioId,
        String estadioNombre,
        int capacidadEstadio) {

    public static PartidoRespuesta desde(Partido partido) {
        return new PartidoRespuesta(
                partido.getId(),
                partido.getEquipoLocal(),
                partido.getEquipoVisitante(),
                partido.getComienzaEn(),
                partido.getEstado().name(),
                partido.getEstadio().getId(),
                partido.getEstadio().getNombre(),
                partido.getEstadio().getCapacidad());
    }
}
