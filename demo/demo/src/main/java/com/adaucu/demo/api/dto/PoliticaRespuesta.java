package com.adaucu.demo.api.dto;

import com.adaucu.demo.precios.PoliticaDePrecio;

public record PoliticaRespuesta(String nombre, String descripcion) {

    public static PoliticaRespuesta desde(PoliticaDePrecio politica) {
        return new PoliticaRespuesta(politica.nombre(), politica.descripcion());
    }
}
