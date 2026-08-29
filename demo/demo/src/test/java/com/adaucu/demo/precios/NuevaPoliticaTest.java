package com.adaucu.demo.precios;

import static org.assertj.core.api.Assertions.assertThat;

import com.adaucu.demo.dominio.Estadio;
import com.adaucu.demo.dominio.Partido;
import com.adaucu.demo.dominio.RolUsuario;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Demuestra RNF-03: la politica "recargo-fijo-2x" se declara solo dentro de
 * este test, como un @Bean mas de tipo PoliticaDePrecio. RegistroDePoliticas
 * la recibe igual que a cualquier otra implementacion (inyecta
 * List<PoliticaDePrecio> completa), sin que ninguna clase de produccion se
 * haya tocado ni recompilado: eso es lo que hace que agregar una politica
 * nueva sea, en la practica, escribir una clase e implementar la interfaz.
 */
@SpringBootTest
class NuevaPoliticaTest {

    @Autowired
    private RegistroDePoliticas registroDePoliticas;

    @TestConfiguration
    static class PoliticaDePruebaConfig {

        @Bean
        PoliticaDePrecio politicaRecargoFijoDoble() {
            return new PoliticaRecargoFijoDoble();
        }
    }

    static class PoliticaRecargoFijoDoble implements PoliticaDePrecio {

        @Override
        public String nombre() {
            return "recargo-fijo-2x";
        }

        @Override
        public String descripcion() {
            return "Politica de prueba: cobra el doble del precio base.";
        }

        @Override
        public Dinero calcular(ContextoDeCompra contexto) {
            BigDecimal total = contexto.asientos().stream()
                    .map(ItemDeAsiento::precioBase)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .multiply(BigDecimal.valueOf(2));
            return Dinero.de(total);
        }
    }

    @Test
    void unaPoliticaDeclaradaSoloEnElTest_quedaDisponibleEnElRegistroSinTocarProduccion() {
        assertThat(registroDePoliticas.disponibles())
                .extracting(PoliticaDePrecio::nombre)
                .contains("precio-base", "descuento-socio", "demanda", "recargo-fijo-2x");

        PoliticaDePrecio resuelta = registroDePoliticas.resolver("recargo-fijo-2x");

        Estadio estadio = new Estadio("Estadio de prueba", 100);
        Partido partido = new Partido("Local", "Visitante", Instant.now(), estadio);
        ContextoDeCompra contexto = new ContextoDeCompra(
                partido,
                List.of(new ItemDeAsiento(1L, "Popular", new BigDecimal("100.00"))),
                RolUsuario.HINCHA,
                0, 100,
                Instant.now());

        Dinero resultado = resuelta.calcular(contexto);
        assertThat(resultado.monto()).isEqualByComparingTo("200.00");
    }
}
