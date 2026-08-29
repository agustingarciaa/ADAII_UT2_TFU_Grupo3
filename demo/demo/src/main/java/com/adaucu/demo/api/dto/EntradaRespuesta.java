package com.adaucu.demo.api.dto;

import com.adaucu.demo.dominio.Entrada;
import java.time.Instant;

public record EntradaRespuesta(
        Long id,
        Long asientoId,
        Long partidoId,
        String codigo,
        Instant emitidaEn) {

    public static EntradaRespuesta desde(Entrada entrada) {
        return new EntradaRespuesta(
                entrada.getId(),
                entrada.getAsiento().getId(),
                entrada.getPartido().getId(),
                entrada.getCodigo(),
                entrada.getEmitidaEn());
    }
}
