package com.adaucu.demo.api.dto;

import com.adaucu.demo.config.ParametrosDeVenta;

/**
 * Expone, de solo lectura, los parametros que el proceso resolvio al
 * arrancar - evidencia verificable de que RNF-04/RNF-05 se satisfacen con
 * binding en tiempo de configuracion y no con constantes en el codigo.
 */
public record ConfiguracionRespuesta(
        int maxEntradasPorCompra,
        int maxComprasSimultaneasPorUsuario,
        String politicaPorDefecto,
        int minutosDeVigenciaDeReserva) {

    public static ConfiguracionRespuesta desde(ParametrosDeVenta parametros) {
        return new ConfiguracionRespuesta(
                parametros.maxEntradasPorCompra(),
                parametros.maxComprasSimultaneasPorUsuario(),
                parametros.politicaPorDefecto(),
                parametros.minutosDeVigenciaDeReserva());
    }
}
