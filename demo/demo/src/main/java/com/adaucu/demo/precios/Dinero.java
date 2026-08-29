package com.adaucu.demo.precios;

import java.math.BigDecimal;

public record Dinero(BigDecimal monto, String moneda) {

    public static Dinero de(BigDecimal monto) {
        return new Dinero(monto, "UYU");
    }

    public Dinero sumar(Dinero otro) {
        if (!moneda.equals(otro.moneda())) {
            throw new IllegalArgumentException("No se pueden sumar montos en monedas distintas");
        }
        return new Dinero(monto.add(otro.monto()), moneda);
    }
}
