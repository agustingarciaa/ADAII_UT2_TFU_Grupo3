package com.adaucu.demo.ventas;

import java.util.List;

/**
 * Se lanza cuando la escritura condicionada por timestamp detecta que un
 * asiento cambio entre la lectura del cliente y el intento de reserva/compra:
 * es la deteccion de estado desprotegido que exige RNF-01.
 */
public class ConflictoDeEstadoException extends RuntimeException {

    private final Long reservaId;
    private final List<AsientoEnConflicto> asientosEnConflicto;

    public ConflictoDeEstadoException(Long reservaId, List<AsientoEnConflicto> asientosEnConflicto) {
        super("Uno o mas asientos cambiaron de estado antes de poder completar la operacion");
        this.reservaId = reservaId;
        this.asientosEnConflicto = asientosEnConflicto;
    }

    public Long getReservaId() {
        return reservaId;
    }

    public List<AsientoEnConflicto> getAsientosEnConflicto() {
        return asientosEnConflicto;
    }
}
