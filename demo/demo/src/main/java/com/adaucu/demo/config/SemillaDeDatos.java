package com.adaucu.demo.config;

import com.adaucu.demo.dominio.Asiento;
import com.adaucu.demo.dominio.Estadio;
import com.adaucu.demo.dominio.Partido;
import com.adaucu.demo.repositorio.AsientoRepositorio;
import com.adaucu.demo.repositorio.EstadioRepositorio;
import com.adaucu.demo.repositorio.PartidoRepositorio;
import com.adaucu.demo.ventas.ProveedorDeMarcaTiempo;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Datos de ejemplo para desarrollo: un estadio chico, un partido y sus
 * asientos. Solo corre si la base esta vacia, para no duplicar datos en
 * reinicios sucesivos.
 */
@Component
@Profile("!test")
public class SemillaDeDatos implements CommandLineRunner {

    private static final String[] SECTORES = {"Popular", "Platea", "Tribuna"};
    private static final int FILAS_POR_SECTOR = 3;
    private static final int ASIENTOS_POR_FILA = 5;

    private final EstadioRepositorio estadioRepositorio;
    private final PartidoRepositorio partidoRepositorio;
    private final AsientoRepositorio asientoRepositorio;
    private final ProveedorDeMarcaTiempo proveedorDeMarcaTiempo;

    public SemillaDeDatos(EstadioRepositorio estadioRepositorio,
                           PartidoRepositorio partidoRepositorio,
                           AsientoRepositorio asientoRepositorio,
                           ProveedorDeMarcaTiempo proveedorDeMarcaTiempo) {
        this.estadioRepositorio = estadioRepositorio;
        this.partidoRepositorio = partidoRepositorio;
        this.asientoRepositorio = asientoRepositorio;
        this.proveedorDeMarcaTiempo = proveedorDeMarcaTiempo;
    }

    @Override
    public void run(String... args) {
        if (partidoRepositorio.count() > 0) {
            return;
        }

        int capacidad = SECTORES.length * FILAS_POR_SECTOR * ASIENTOS_POR_FILA;
        Estadio estadio = estadioRepositorio.save(new Estadio("Estadio Centenario", capacidad));

        Instant comienzo = Instant.now().plus(7, ChronoUnit.DAYS);
        Partido partido = partidoRepositorio.save(new Partido("Nacional", "Penarol", comienzo, estadio));

        List<Asiento> asientos = new ArrayList<>();
        for (String sector : SECTORES) {
            BigDecimal precioBase = precioPorSector(sector);
            for (int fila = 1; fila <= FILAS_POR_SECTOR; fila++) {
                for (int numero = 1; numero <= ASIENTOS_POR_FILA; numero++) {
                    Instant marcaInicial = proveedorDeMarcaTiempo.marcaActual();
                    asientos.add(new Asiento(partido, sector, "F" + fila, numero, precioBase, marcaInicial));
                }
            }
        }
        asientoRepositorio.saveAll(asientos);
    }

    private BigDecimal precioPorSector(String sector) {
        return switch (sector) {
            case "Platea" -> new BigDecimal("2500.00");
            case "Tribuna" -> new BigDecimal("1500.00");
            default -> new BigDecimal("800.00");
        };
    }
}
