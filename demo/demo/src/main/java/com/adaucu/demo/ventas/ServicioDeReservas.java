package com.adaucu.demo.ventas;

import com.adaucu.demo.api.EntidadNoEncontradaException;
import com.adaucu.demo.config.ParametrosDeVenta;
import com.adaucu.demo.dominio.Asiento;
import com.adaucu.demo.dominio.EstadoAsiento;
import com.adaucu.demo.dominio.EstadoReserva;
import com.adaucu.demo.dominio.Partido;
import com.adaucu.demo.dominio.Reserva;
import com.adaucu.demo.dominio.Usuario;
import com.adaucu.demo.repositorio.AsientoRepositorio;
import com.adaucu.demo.repositorio.PartidoRepositorio;
import com.adaucu.demo.repositorio.ReservaRepositorio;
import java.time.Instant;
import java.util.List;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orquesta la creacion de una reserva aplicando las tres tacticas elegidas:
 * timestamp para detectar conflictos sobre los asientos (RNF-01, RNF-02),
 * y los limites de RNF-04/RNF-05 leidos desde ParametrosDeVenta, que se
 * resuelven en tiempo de arranque (tactica de binding en configuracion).
 *
 * Este metodo NO es @Transactional: delega el trabajo transaccional a
 * EjecutorDeReservas y, solo si esa transaccion termina en conflicto,
 * registra la auditoria del rechazo (AuditoriaDeReservas) ya con esa
 * transaccion cerrada. Ver el comentario de EjecutorDeReservas para el
 * porque (SQLite y su lock de escritura por archivo).
 */
@Service
public class ServicioDeReservas {

    /**
     * SQLite solo admite un escritor a la vez sobre todo el archivo: dos
     * transacciones que escriben al mismo tiempo (aunque sobre asientos
     * distintos) pueden chocar con SQLITE_BUSY. Este reintento acotado es
     * puramente sobre esa contencion fisica de E/S; la deteccion logica de
     * conflicto (timestamp) sigue resolviendose igual en cada intento.
     */
    private static final int MAX_REINTENTOS_POR_BLOQUEO = 15;

    private final AsientoRepositorio asientoRepositorio;
    private final ReservaRepositorio reservaRepositorio;
    private final PartidoRepositorio partidoRepositorio;
    private final ProveedorDeMarcaTiempo proveedorDeMarcaTiempo;
    private final EjecutorDeReservas ejecutorDeReservas;
    private final AuditoriaDeReservas auditoriaDeReservas;
    private final ParametrosDeVenta parametros;

    public ServicioDeReservas(AsientoRepositorio asientoRepositorio,
                               ReservaRepositorio reservaRepositorio,
                               PartidoRepositorio partidoRepositorio,
                               ProveedorDeMarcaTiempo proveedorDeMarcaTiempo,
                               EjecutorDeReservas ejecutorDeReservas,
                               AuditoriaDeReservas auditoriaDeReservas,
                               ParametrosDeVenta parametros) {
        this.asientoRepositorio = asientoRepositorio;
        this.reservaRepositorio = reservaRepositorio;
        this.partidoRepositorio = partidoRepositorio;
        this.proveedorDeMarcaTiempo = proveedorDeMarcaTiempo;
        this.ejecutorDeReservas = ejecutorDeReservas;
        this.auditoriaDeReservas = auditoriaDeReservas;
        this.parametros = parametros;
    }

    public Reserva crear(Usuario usuario, Long partidoId, List<PeticionDeAsiento> pedidos, String nombrePolitica) {
        if (pedidos == null || pedidos.isEmpty()) {
            throw new IllegalArgumentException("La reserva debe incluir al menos un asiento");
        }
        if (pedidos.size() > parametros.maxEntradasPorCompra()) {
            throw new LimiteExcedidoException(
                    "No se pueden reservar mas de " + parametros.maxEntradasPorCompra() + " entradas por compra");
        }

        Partido partido = partidoRepositorio.findById(partidoId)
                .orElseThrow(() -> new EntidadNoEncontradaException("No existe el partido " + partidoId));

        try {
            return intentarConReintentoPorBloqueo(usuario, partido, pedidos, nombrePolitica);
        } catch (ConflictoDeEstadoException ex) {
            Long reservaRechazadaId = auditoriaDeReservas.registrarRechazo(
                    usuario, partido, proveedorDeMarcaTiempo.marcaActual());
            throw new ConflictoDeEstadoException(reservaRechazadaId, ex.getAsientosEnConflicto());
        }
    }

    private Reserva intentarConReintentoPorBloqueo(Usuario usuario, Partido partido,
                                                    List<PeticionDeAsiento> pedidos, String nombrePolitica) {
        for (int intento = 1; intento <= MAX_REINTENTOS_POR_BLOQUEO; intento++) {
            try {
                return ejecutorDeReservas.intentar(usuario, partido, pedidos, nombrePolitica);
            } catch (CannotAcquireLockException ex) {
                if (intento == MAX_REINTENTOS_POR_BLOQUEO) {
                    throw ex;
                }
                esperarUnPoco(intento);
            }
        }
        throw new IllegalStateException("No deberia alcanzarse");
    }

    private void esperarUnPoco(int intento) {
        try {
            long baseMs = Math.min(50L * intento, 500L);
            long conJitter = baseMs + (long) (Math.random() * 50);
            Thread.sleep(conJitter);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Transactional
    public void cancelar(Reserva reserva) {
        if (reserva.getEstado() != EstadoReserva.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden cancelar reservas PENDIENTES");
        }
        for (var reservaAsiento : reserva.getAsientos()) {
            Asiento asiento = reservaAsiento.getAsiento();
            asientoRepositorio.actualizarEstadoSiLaMarcaCoincide(
                    asiento.getId(), asiento.getMarcaTiempo(), EstadoAsiento.LIBRE, proveedorDeMarcaTiempo.marcaActual());
        }
        reserva.setEstado(EstadoReserva.CANCELADA);
        reservaRepositorio.save(reserva);
    }
}
