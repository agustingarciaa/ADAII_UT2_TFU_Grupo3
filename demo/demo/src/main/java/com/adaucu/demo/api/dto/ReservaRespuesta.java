package com.adaucu.demo.api.dto;

import com.adaucu.demo.dominio.Reserva;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ReservaRespuesta(
        Long id,
        Long partidoId,
        String estado,
        Instant creadaEn,
        Instant expiraEn,
        String politicaAplicada,
        BigDecimal montoTotal,
        List<Long> asientoIds) {

    public static ReservaRespuesta desde(Reserva reserva) {
        List<Long> asientoIds = reserva.getAsientos().stream()
                .map(ra -> ra.getAsiento().getId())
                .toList();
        return new ReservaRespuesta(
                reserva.getId(),
                reserva.getPartido().getId(),
                reserva.getEstado().name(),
                reserva.getCreadaEn(),
                reserva.getExpiraEn(),
                reserva.getPoliticaAplicada(),
                reserva.getMontoTotal(),
                asientoIds);
    }
}
