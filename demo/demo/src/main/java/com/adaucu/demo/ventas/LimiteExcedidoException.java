package com.adaucu.demo.ventas;

public class LimiteExcedidoException extends RuntimeException {

    public LimiteExcedidoException(String mensaje) {
        super(mensaje);
    }
}
