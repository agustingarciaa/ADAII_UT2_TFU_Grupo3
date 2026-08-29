package com.adaucu.demo.ventas;

import com.adaucu.demo.dominio.EstadoReserva;
import com.adaucu.demo.dominio.Partido;
import com.adaucu.demo.dominio.Reserva;
import com.adaucu.demo.dominio.Usuario;
import com.adaucu.demo.repositorio.ReservaRepositorio;
import java.time.Instant;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registra el rechazo en una transaccion independiente de la que detecto el
 * conflicto: esa transaccion hace rollback de las escrituras sobre los
 * asientos (para no dejar ninguno a medio reservar), pero el hecho de que la
 * reserva fue rechazada debe sobrevivir a ese rollback para poder auditarlo.
 */
@Component
public class AuditoriaDeReservas {

    private final ReservaRepositorio reservaRepositorio;

    public AuditoriaDeReservas(ReservaRepositorio reservaRepositorio) {
        this.reservaRepositorio = reservaRepositorio;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long registrarRechazo(Usuario usuario, Partido partido, Instant ahora) {
        Reserva reserva = new Reserva(usuario, partido, ahora, ahora);
        reserva.setEstado(EstadoReserva.RECHAZADA);
        reservaRepositorio.save(reserva);
        return reserva.getId();
    }
}
