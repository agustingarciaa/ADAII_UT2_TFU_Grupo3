package com.adaucu.demo.ventas;

import com.adaucu.demo.api.EntidadNoEncontradaException;
import com.adaucu.demo.config.ParametrosDeVenta;
import com.adaucu.demo.dominio.Asiento;
import com.adaucu.demo.dominio.Partido;
import com.adaucu.demo.dominio.Reserva;
import com.adaucu.demo.dominio.Usuario;
import com.adaucu.demo.precios.Dinero;
import com.adaucu.demo.repositorio.AsientoRepositorio;
import com.adaucu.demo.repositorio.ReservaRepositorio;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Hace, en una unica transaccion, los intentos de escritura CAS sobre los
 * asientos y - si todos coinciden - persiste la Reserva. Vive separado de
 * ServicioDeReservas a proposito: SQLite mantiene el lock de escritura del
 * archivo durante toda esta transaccion, asi que el registro de auditoria del
 * rechazo (AuditoriaDeReservas) no puede escribirse mientras esta transaccion
 * sigue abierta - haria falta una segunda conexion compitiendo por el mismo
 * lock que la primera todavia sostiene, un auto-deadlock. Por eso
 * ServicioDeReservas llama a este metodo, deja que la transaccion termine
 * (commit o rollback) y solo despues, sin ninguna transaccion ambiente,
 * registra la auditoria.
 */
@Component
class EjecutorDeReservas {

    private final AsientoRepositorio asientoRepositorio;
    private final ReservaRepositorio reservaRepositorio;
    private final ProveedorDeMarcaTiempo proveedorDeMarcaTiempo;
    private final MotorDeVentas motorDeVentas;
    private final ParametrosDeVenta parametros;

    EjecutorDeReservas(AsientoRepositorio asientoRepositorio,
                        ReservaRepositorio reservaRepositorio,
                        ProveedorDeMarcaTiempo proveedorDeMarcaTiempo,
                        MotorDeVentas motorDeVentas,
                        ParametrosDeVenta parametros) {
        this.asientoRepositorio = asientoRepositorio;
        this.reservaRepositorio = reservaRepositorio;
        this.proveedorDeMarcaTiempo = proveedorDeMarcaTiempo;
        this.motorDeVentas = motorDeVentas;
        this.parametros = parametros;
    }

    @Transactional
    Reserva intentar(Usuario usuario, Partido partido, List<PeticionDeAsiento> pedidos, String nombrePolitica) {
        Instant ahora = proveedorDeMarcaTiempo.marcaActual();

        long activas = reservaRepositorio.contarActivasPorUsuario(usuario.getId(), ahora);
        if (activas >= parametros.maxComprasSimultaneasPorUsuario()) {
            throw new LimiteExcedidoException(
                    "Ya tenes " + activas + " reservas activas; el maximo permitido es "
                            + parametros.maxComprasSimultaneasPorUsuario());
        }

        List<PeticionDeAsiento> ordenados = pedidos.stream()
                .sorted(Comparator.comparing(PeticionDeAsiento::asientoId))
                .toList();

        List<Asiento> asientosReservados = new ArrayList<>();
        List<AsientoEnConflicto> conflictos = new ArrayList<>();

        for (PeticionDeAsiento pedido : ordenados) {
            Instant nuevaMarca = proveedorDeMarcaTiempo.marcaActual();
            int filasActualizadas = asientoRepositorio.actualizarEstadoSiLaMarcaCoincide(
                    pedido.asientoId(), pedido.marcaTiempo(), com.adaucu.demo.dominio.EstadoAsiento.RESERVADO, nuevaMarca);

            Asiento asiento = asientoRepositorio.findById(pedido.asientoId())
                    .orElseThrow(() -> new EntidadNoEncontradaException("No existe el asiento " + pedido.asientoId()));

            if (filasActualizadas == 0) {
                conflictos.add(new AsientoEnConflicto(pedido.asientoId(), asiento.getMarcaTiempo()));
            } else {
                asientosReservados.add(asiento);
            }
        }

        if (!conflictos.isEmpty()) {
            // Lanzar aca hace rollback de todas las escrituras CAS de
            // arriba (incluidas las que si habian coincidido), asi que
            // ningun asiento queda a medio reservar.
            throw new ConflictoDeEstadoException(null, conflictos);
        }

        Instant expiraEn = ahora.plus(parametros.minutosDeVigenciaDeReserva(), ChronoUnit.MINUTES);
        Reserva reserva = new Reserva(usuario, partido, ahora, expiraEn);
        asientosReservados.forEach(reserva::agregarAsiento);

        Dinero monto = motorDeVentas.cotizar(partido, asientosReservados, usuario.getRol(), nombrePolitica, ahora);
        reserva.setMontoTotal(monto.monto());
        reserva.setPoliticaAplicada(motorDeVentas.resolverNombrePolitica(nombrePolitica));

        return reservaRepositorio.save(reserva);
    }
}
