package com.adaucu.demo.config;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.adaucu.demo.DemoApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * RNF-04/05: @Validated en ParametrosDeVenta hace que un valor invalido en la
 * configuracion externa impida el arranque del proceso con un mensaje claro,
 * en lugar de dejar el sistema funcionando con un limite sin sentido (0
 * entradas por compra). Esto es lo que evita que un operador sin
 * conocimiento tecnico deje el sistema en un estado invalido sin darse
 * cuenta.
 */
class ParametrosDeVentaInvalidaTest {

    @Test
    void unMaximoDeEntradasPorCompraInvalido_impideElArranque() {
        assertThatThrownBy(() -> {
            ConfigurableApplicationContext contexto = new SpringApplicationBuilder(DemoApplication.class)
                    .web(WebApplicationType.NONE)
                    .profiles("test")
                    .run("--ventas.max-entradas-por-compra=0");
            contexto.close();
        }).isInstanceOf(Exception.class);
    }
}
