package com.adaucu.demo.ventas;

import static org.assertj.core.api.Assertions.assertThat;

import com.adaucu.demo.dominio.Asiento;
import com.adaucu.demo.dominio.Entrada;
import com.adaucu.demo.dominio.Estadio;
import com.adaucu.demo.dominio.EstadoReserva;
import com.adaucu.demo.dominio.Partido;
import com.adaucu.demo.dominio.Reserva;
import com.adaucu.demo.dominio.RolUsuario;
import com.adaucu.demo.dominio.Usuario;
import com.adaucu.demo.repositorio.AsientoRepositorio;
import com.adaucu.demo.repositorio.EntradaRepositorio;
import com.adaucu.demo.repositorio.EstadioRepositorio;
import com.adaucu.demo.repositorio.PartidoRepositorio;
import com.adaucu.demo.repositorio.ReservaRepositorio;
import com.adaucu.demo.repositorio.UsuarioRepositorio;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Verifica RNF-01 y RNF-02 ejercitando directamente la tactica de timestamp:
 * dos "pedidos" concurrentes leen el mismo asiento (con la misma
 * marcaTiempo) e intentan reservarlo al mismo tiempo. Si se quitara la
 * escritura condicionada por marca de AsientoRepositorio, este test fallaria
 * porque ambos pedidos terminarian aceptados.
 */
@SpringBootTest
class ReservaConcurrenteTest {

    @Autowired
    private ServicioDeReservas servicioDeReservas;
    @Autowired
    private ServicioDeCompras servicioDeCompras;
    @Autowired
    private EstadioRepositorio estadioRepositorio;
    @Autowired
    private PartidoRepositorio partidoRepositorio;
    @Autowired
    private AsientoRepositorio asientoRepositorio;
    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    @Autowired
    private ReservaRepositorio reservaRepositorio;
    @Autowired
    private EntradaRepositorio entradaRepositorio;

    @Test
    void dosPedidosConcurrentesPorElMismoAsiento_soloUnoEsAceptado() throws InterruptedException {
        Estadio estadio = estadioRepositorio.save(new Estadio("Estadio de prueba", 100));
        Partido partido = partidoRepositorio.save(new Partido("Local", "Visitante", Instant.now(), estadio));
        Asiento asiento = asientoRepositorio.save(
                new Asiento(partido, "Popular", "F1", 1, new BigDecimal("500.00"), Instant.now()));

        Usuario usuarioA = usuarioRepositorio.save(
                new Usuario("a@test.com", "hash", "salt", "Usuario A", RolUsuario.HINCHA));
        Usuario usuarioB = usuarioRepositorio.save(
                new Usuario("b@test.com", "hash", "salt", "Usuario B", RolUsuario.HINCHA));

        // Ambos "leyeron" el asiento con la misma marca, como si hubieran
        // consultado GET /partidos/{id}/asientos al mismo tiempo.
        Instant marcaLeida = asientoRepositorio.findById(asiento.getId()).orElseThrow().getMarcaTiempo();

        AtomicInteger exitos = new AtomicInteger();
        AtomicInteger conflictos = new AtomicInteger();
        CountDownLatch listos = new CountDownLatch(2);
        CountDownLatch largada = new CountDownLatch(1);

        ExecutorService pool = Executors.newFixedThreadPool(2);

        Runnable pedidoA = () -> intentarReservar(usuarioA, partido.getId(), asiento.getId(), marcaLeida,
                listos, largada, exitos, conflictos);
        Runnable pedidoB = () -> intentarReservar(usuarioB, partido.getId(), asiento.getId(), marcaLeida,
                listos, largada, exitos, conflictos);

        pool.submit(pedidoA);
        pool.submit(pedidoB);

        listos.await(5, TimeUnit.SECONDS);
        largada.countDown();
        pool.shutdown();
        assertThat(pool.awaitTermination(10, TimeUnit.SECONDS)).isTrue();

        assertThat(exitos.get()).isEqualTo(1);
        assertThat(conflictos.get()).isEqualTo(1);

        Asiento asientoFinal = asientoRepositorio.findById(asiento.getId()).orElseThrow();
        assertThat(asientoFinal.getEstado().name()).isEqualTo("RESERVADO");

        long reservasRechazadas = reservaRepositorio.findAll().stream()
                .filter(r -> r.getEstado() == EstadoReserva.RECHAZADA)
                .count();
        assertThat(reservasRechazadas).isEqualTo(1);

        // RNF-02: al confirmar la compra de la reserva ganadora, nunca se
        // emite mas de una entrada para el asiento.
        Reserva reservaGanadora = reservaRepositorio.findAll().stream()
                .filter(r -> r.getEstado() == EstadoReserva.PENDIENTE)
                .findFirst()
                .orElseThrow();
        Usuario duenio = reservaGanadora.getUsuario();
        servicioDeCompras.confirmar(reservaGanadora, duenio);

        List<Entrada> entradasDelAsiento = entradaRepositorio.findAll().stream()
                .filter(e -> e.getAsiento().getId().equals(asiento.getId()))
                .toList();
        assertThat(entradasDelAsiento).hasSize(1);
    }

    private void intentarReservar(Usuario usuario, Long partidoId, Long asientoId, Instant marcaLeida,
                                   CountDownLatch listos, CountDownLatch largada,
                                   AtomicInteger exitos, AtomicInteger conflictos) {
        try {
            listos.countDown();
            largada.await();
            List<PeticionDeAsiento> pedido = List.of(new PeticionDeAsiento(asientoId, marcaLeida));
            servicioDeReservas.crear(usuario, partidoId, pedido, null);
            exitos.incrementAndGet();
        } catch (ConflictoDeEstadoException ex) {
            conflictos.incrementAndGet();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
