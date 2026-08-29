package com.adaucu.demo.ventas;

import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Component;

/**
 * Pieza central de la tactica de deteccion de estados desprotegidos: entrega
 * marcas de tiempo estrictamente crecientes. Sin esta garantia, la resolucion
 * del reloj del sistema operativo podria hacer que dos escrituras sucesivas
 * compartan el mismo Instant y la comparacion de marcas dejara de discriminar
 * entre "nadie toco esto" y "alguien ya lo toco".
 */
@Component
public class ProveedorDeMarcaTiempo {

    private final Clock reloj;
    private Instant ultimaEmitida = Instant.MIN;

    public ProveedorDeMarcaTiempo(Clock reloj) {
        this.reloj = reloj;
    }

    public synchronized Instant marcaActual() {
        Instant ahora = reloj.instant();
        if (!ahora.isAfter(ultimaEmitida)) {
            ahora = ultimaEmitida.plusNanos(1);
        }
        ultimaEmitida = ahora;
        return ahora;
    }
}
