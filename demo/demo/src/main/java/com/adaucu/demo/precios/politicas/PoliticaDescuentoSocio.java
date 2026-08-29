package com.adaucu.demo.precios.politicas;

import com.adaucu.demo.dominio.RolUsuario;
import com.adaucu.demo.precios.ContextoDeCompra;
import com.adaucu.demo.precios.Dinero;
import com.adaucu.demo.precios.PoliticaDePrecio;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class PoliticaDescuentoSocio implements PoliticaDePrecio {

    private static final BigDecimal DESCUENTO_SOCIO = new BigDecimal("0.20");

    @Override
    public String nombre() {
        return "descuento-socio";
    }

    @Override
    public String descripcion() {
        return "Aplica un 20% de descuento sobre el precio base a usuarios con rol SOCIO.";
    }

    @Override
    public Dinero calcular(ContextoDeCompra contexto) {
        BigDecimal total = contexto.asientos().stream()
                .map(item -> item.precioBase())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (contexto.rolUsuario() == RolUsuario.SOCIO) {
            BigDecimal factor = BigDecimal.ONE.subtract(DESCUENTO_SOCIO);
            total = total.multiply(factor);
        }
        return Dinero.de(total);
    }
}
