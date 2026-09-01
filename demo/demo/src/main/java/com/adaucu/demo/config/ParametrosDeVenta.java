package com.adaucu.demo.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Tactica de binding en tiempo de configuracion (categoria "diferir el
 * binding"). Estos valores no estan fijados en el codigo: se resuelven al
 * iniciar el proceso, leyendolos de application.properties o del archivo
 * externo config/ventas.properties, que se importa al arrancar con
 * --spring.config.import=optional:file:./config/ventas.properties. Editar el
 * archivo y reiniciar el servicio alcanza para cambiarlos, sin recompilar ni
 * redesplegar (RNF-04, RNF-05). @Validated hace que un valor invalido impida el arranque con un
 * mensaje claro, en lugar de degradar el sistema en silencio.
 */
@ConfigurationProperties(prefix = "ventas")
@Validated
public record ParametrosDeVenta(

        @Min(1) @Max(50)
        int maxEntradasPorCompra,

        @Min(1) @Max(20)
        int maxComprasSimultaneasPorUsuario,

        @NotBlank
        String politicaPorDefecto,

        @Positive
        int minutosDeVigenciaDeReserva) {
}
