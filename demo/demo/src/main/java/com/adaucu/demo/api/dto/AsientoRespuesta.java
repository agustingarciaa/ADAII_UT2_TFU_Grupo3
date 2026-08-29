package com.adaucu.demo.api.dto;

import com.adaucu.demo.dominio.Asiento;
import java.math.BigDecimal;
import java.time.Instant;

public record AsientoRespuesta(
        Long id,
        String sector,
        String fila,
        int numero,
        String estado,
        BigDecimal precioBase,
        Instant marcaTiempo) {

    public static AsientoRespuesta desde(Asiento asiento) {
        return new AsientoRespuesta(
                asiento.getId(),
                asiento.getSector(),
                asiento.getFila(),
                asiento.getNumero(),
                asiento.getEstado().name(),
                asiento.getPrecioBase(),
                asiento.getMarcaTiempo());
    }
}
