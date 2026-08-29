package com.adaucu.demo.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.adaucu.demo.dominio.Asiento;
import com.adaucu.demo.dominio.Estadio;
import com.adaucu.demo.dominio.Partido;
import com.adaucu.demo.dominio.RolUsuario;
import com.adaucu.demo.dominio.Usuario;
import com.adaucu.demo.repositorio.AsientoRepositorio;
import com.adaucu.demo.repositorio.EstadioRepositorio;
import com.adaucu.demo.repositorio.PartidoRepositorio;
import com.adaucu.demo.repositorio.UsuarioRepositorio;
import com.adaucu.demo.ventas.LimiteExcedidoException;
import com.adaucu.demo.ventas.PeticionDeAsiento;
import com.adaucu.demo.ventas.ServicioDeReservas;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * RNF-04 y RNF-05: los limites operativos vienen de ParametrosDeVenta, que se
 * resuelve leyendo la configuracion externa al arranque (tactica de binding
 * en tiempo de configuracion). Este test fija valores distintos a los de
 * application.properties con @TestPropertySource - equivalente a que un
 * operador edite config/ventas.properties y reinicie el servicio - y
 * verifica que el motor de ventas los respeta sin que haya sido necesario
 * tocar codigo.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "ventas.max-entradas-por-compra=2",
        "ventas.max-compras-simultaneas-por-usuario=1"
})
class ParametrosDeVentaLimitesTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ServicioDeReservas servicioDeReservas;
    @Autowired
    private EstadioRepositorio estadioRepositorio;
    @Autowired
    private PartidoRepositorio partidoRepositorio;
    @Autowired
    private AsientoRepositorio asientoRepositorio;
    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Test
    void elEndpointDeConfiguracionReflejaLosValoresResueltosAlArrancar() throws Exception {
        mockMvc.perform(get("/api/v1/configuracion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maxEntradasPorCompra").value(2))
                .andExpect(jsonPath("$.maxComprasSimultaneasPorUsuario").value(1));
    }

    @Test
    void unaTerceraEntradaEnLaMismaCompra_esRechazadaPorElLimiteConfigurado() {
        Estadio estadio = estadioRepositorio.save(new Estadio("Estadio de prueba", 100));
        Partido partido = partidoRepositorio.save(new Partido("Local", "Visitante", Instant.now(), estadio));
        Usuario usuario = usuarioRepositorio.save(
                new Usuario("limite@test.com", "hash", "salt", "Usuario Limite", RolUsuario.HINCHA));

        List<PeticionDeAsiento> tresAsientos = crearAsientos(partido, 3).stream()
                .map(a -> new PeticionDeAsiento(a.getId(), a.getMarcaTiempo()))
                .toList();

        assertThatThrownBy(() -> servicioDeReservas.crear(usuario, partido.getId(), tresAsientos, null))
                .isInstanceOf(LimiteExcedidoException.class);
    }

    @Test
    void unaSegundaReservaActivaDelMismoUsuario_esRechazadaPorElLimiteConfigurado() {
        Estadio estadio = estadioRepositorio.save(new Estadio("Estadio de prueba", 100));
        Partido partido = partidoRepositorio.save(new Partido("Local", "Visitante", Instant.now(), estadio));
        Usuario usuario = usuarioRepositorio.save(
                new Usuario("simultanea@test.com", "hash", "salt", "Usuario Simultanea", RolUsuario.HINCHA));

        Asiento primero = crearAsientos(partido, 1).get(0);
        servicioDeReservas.crear(usuario, partido.getId(),
                List.of(new PeticionDeAsiento(primero.getId(), primero.getMarcaTiempo())), null);

        Asiento segundo = crearAsientos(partido, 1).get(0);
        assertThatThrownBy(() -> servicioDeReservas.crear(usuario, partido.getId(),
                List.of(new PeticionDeAsiento(segundo.getId(), segundo.getMarcaTiempo())), null))
                .isInstanceOf(LimiteExcedidoException.class);
    }

    private static final java.util.concurrent.atomic.AtomicInteger CONTADOR_DE_NUMERO =
            new java.util.concurrent.atomic.AtomicInteger(1);

    private List<Asiento> crearAsientos(Partido partido, int cantidad) {
        List<Asiento> asientos = new java.util.ArrayList<>();
        for (int i = 0; i < cantidad; i++) {
            int numero = CONTADOR_DE_NUMERO.getAndIncrement();
            asientos.add(asientoRepositorio.save(new Asiento(
                    partido, "Popular", "F1", numero, new BigDecimal("500.00"), Instant.now())));
        }
        return asientos;
    }
}
