package com.adaucu.demo.precios;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Indexa por nombre todas las implementaciones de PoliticaDePrecio presentes
 * en el classpath. Spring inyecta automaticamente la lista completa de beans
 * que implementan la interfaz: para que el motor de ventas empiece a usar una
 * politica nueva alcanza con anotarla @Component, sin tocar este registro ni
 * el motor.
 */
@Component
public class RegistroDePoliticas {

    private final Map<String, PoliticaDePrecio> politicasPorNombre = new HashMap<>();

    public RegistroDePoliticas(List<PoliticaDePrecio> politicas) {
        for (PoliticaDePrecio politica : politicas) {
            PoliticaDePrecio anterior = politicasPorNombre.putIfAbsent(politica.nombre(), politica);
            if (anterior != null) {
                throw new IllegalStateException(
                        "Hay mas de una PoliticaDePrecio registrada con el nombre '" + politica.nombre() + "'");
            }
        }
    }

    public PoliticaDePrecio resolver(String nombre) {
        PoliticaDePrecio politica = politicasPorNombre.get(nombre);
        if (politica == null) {
            throw new PoliticaNoEncontradaException(nombre);
        }
        return politica;
    }

    public Collection<PoliticaDePrecio> disponibles() {
        return politicasPorNombre.values();
    }
}
