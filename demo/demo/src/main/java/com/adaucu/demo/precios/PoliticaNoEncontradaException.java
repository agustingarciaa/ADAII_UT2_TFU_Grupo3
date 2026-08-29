package com.adaucu.demo.precios;

public class PoliticaNoEncontradaException extends RuntimeException {

    public PoliticaNoEncontradaException(String nombre) {
        super("No existe una politica de precio registrada con nombre '" + nombre + "'");
    }
}
