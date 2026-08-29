package com.adaucu.demo.ventas;

import com.adaucu.demo.dominio.Asiento;
import com.adaucu.demo.dominio.Compra;
import com.adaucu.demo.dominio.Entrada;
import com.adaucu.demo.dominio.EstadoAsiento;
import com.adaucu.demo.dominio.EstadoReserva;
import com.adaucu.demo.dominio.Reserva;
import com.adaucu.demo.dominio.Usuario;
import com.adaucu.demo.repositorio.AsientoRepositorio;
import com.adaucu.demo.repositorio.CompraRepositorio;
import com.adaucu.demo.repositorio.EntradaRepositorio;
import com.adaucu.demo.repositorio.ReservaRepositorio;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Confirma una reserva PENDIENTE como Compra y emite sus Entrada. El paso de
 * RESERVADO a VENDIDO se hace con la misma escritura condicionada por
 * timestamp que usa ServicioDeReservas, y el UNIQUE(asiento_id) de Entrada es
 * la ultima linea de defensa de RNF-02.
 */
@Service
public class ServicioDeCompras {

    private final AsientoRepositorio asientoRepositorio;
    private final ReservaRepositorio reservaRepositorio;
    private final CompraRepositorio compraRepositorio;
    private final EntradaRepositorio entradaRepositorio;
    private final ProveedorDeMarcaTiempo proveedorDeMarcaTiempo;

    public ServicioDeCompras(AsientoRepositorio asientoRepositorio,
                              ReservaRepositorio reservaRepositorio,
                              CompraRepositorio compraRepositorio,
                              EntradaRepositorio entradaRepositorio,
                              ProveedorDeMarcaTiempo proveedorDeMarcaTiempo) {
        this.asientoRepositorio = asientoRepositorio;
        this.reservaRepositorio = reservaRepositorio;
        this.compraRepositorio = compraRepositorio;
        this.entradaRepositorio = entradaRepositorio;
        this.proveedorDeMarcaTiempo = proveedorDeMarcaTiempo;
    }

    @Transactional
    public Compra confirmar(Reserva reserva, Usuario usuario) {
        if (reserva.getEstado() != EstadoReserva.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden confirmar reservas PENDIENTES");
        }
        Instant ahora = proveedorDeMarcaTiempo.marcaActual();
        if (reserva.estaVencida(ahora)) {
            reserva.setEstado(EstadoReserva.EXPIRADA);
            reservaRepositorio.save(reserva);
            throw new IllegalStateException("La reserva vencio antes de confirmar el pago");
        }

        List<AsientoEnConflicto> conflictos = new ArrayList<>();
        List<Asiento> asientosVendidos = new ArrayList<>();

        for (var reservaAsiento : reserva.getAsientos()) {
            Asiento asiento = reservaAsiento.getAsiento();
            Instant nuevaMarca = proveedorDeMarcaTiempo.marcaActual();
            int filas = asientoRepositorio.actualizarEstadoSiLaMarcaCoincide(
                    asiento.getId(), asiento.getMarcaTiempo(), EstadoAsiento.VENDIDO, nuevaMarca);

            Asiento actual = asientoRepositorio.findById(asiento.getId()).orElseThrow();
            if (filas == 0) {
                conflictos.add(new AsientoEnConflicto(asiento.getId(), actual.getMarcaTiempo()));
            } else {
                asientosVendidos.add(actual);
            }
        }

        if (!conflictos.isEmpty()) {
            throw new ConflictoDeEstadoException(reserva.getId(), conflictos);
        }

        Compra compra = new Compra(reserva, usuario, reserva.getMontoTotal(), ahora);
        compraRepositorio.save(compra);

        for (Asiento asiento : asientosVendidos) {
            String codigo = UUID.randomUUID().toString();
            entradaRepositorio.save(new Entrada(compra, asiento, reserva.getPartido(), codigo, ahora));
        }

        reserva.setEstado(EstadoReserva.CONFIRMADA);
        reservaRepositorio.save(reserva);

        return compra;
    }
}
