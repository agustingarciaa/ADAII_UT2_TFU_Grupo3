package com.adaucu.demo.ventas;

import com.adaucu.demo.config.ParametrosDeVenta;
import com.adaucu.demo.dominio.Asiento;
import com.adaucu.demo.dominio.Partido;
import com.adaucu.demo.dominio.RolUsuario;
import com.adaucu.demo.precios.ContextoDeCompra;
import com.adaucu.demo.precios.Dinero;
import com.adaucu.demo.precios.ItemDeAsiento;
import com.adaucu.demo.precios.PoliticaDePrecio;
import com.adaucu.demo.precios.RegistroDePoliticas;
import com.adaucu.demo.repositorio.EntradaRepositorio;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * El motor de ventas no implementa ninguna formula de precio: depende
 * unicamente de la abstraccion PoliticaDePrecio y de RegistroDePoliticas para
 * resolver, por nombre y en tiempo de ejecucion, cual implementacion usar
 * (tactica de polimorfismo, RNF-03).
 */
@Component
public class MotorDeVentas {

    private final RegistroDePoliticas registroDePoliticas;
    private final EntradaRepositorio entradaRepositorio;
    private final ParametrosDeVenta parametros;

    public MotorDeVentas(RegistroDePoliticas registroDePoliticas,
                          EntradaRepositorio entradaRepositorio,
                          ParametrosDeVenta parametros) {
        this.registroDePoliticas = registroDePoliticas;
        this.entradaRepositorio = entradaRepositorio;
        this.parametros = parametros;
    }

    public Dinero cotizar(Partido partido, List<Asiento> asientos, RolUsuario rolUsuario,
                           String nombrePolitica, Instant momento) {
        String politicaAUsar = (nombrePolitica != null && !nombrePolitica.isBlank())
                ? nombrePolitica
                : parametros.politicaPorDefecto();

        PoliticaDePrecio politica = registroDePoliticas.resolver(politicaAUsar);

        List<ItemDeAsiento> items = asientos.stream()
                .map(a -> new ItemDeAsiento(a.getId(), a.getSector(), a.getPrecioBase()))
                .toList();

        long entradasVendidas = entradaRepositorio.countByAsientoPartidoId(partido.getId());
        long capacidad = partido.getEstadio().getCapacidad();

        ContextoDeCompra contexto = new ContextoDeCompra(
                partido, items, rolUsuario, entradasVendidas, capacidad, momento);

        return politica.calcular(contexto);
    }

    public String resolverNombrePolitica(String nombrePolitica) {
        return (nombrePolitica != null && !nombrePolitica.isBlank())
                ? nombrePolitica
                : parametros.politicaPorDefecto();
    }
}
