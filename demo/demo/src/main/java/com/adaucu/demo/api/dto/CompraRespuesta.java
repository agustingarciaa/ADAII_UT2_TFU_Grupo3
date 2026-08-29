package com.adaucu.demo.api.dto;

import com.adaucu.demo.dominio.Compra;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record CompraRespuesta(
        Long id,
        Long reservaId,
        BigDecimal montoTotal,
        Instant pagadaEn,
        List<EntradaRespuesta> entradas) {

    public static CompraRespuesta desde(Compra compra, List<EntradaRespuesta> entradas) {
        return new CompraRespuesta(
                compra.getId(),
                compra.getReserva().getId(),
                compra.getMontoTotal(),
                compra.getPagadaEn(),
                entradas);
    }
}
