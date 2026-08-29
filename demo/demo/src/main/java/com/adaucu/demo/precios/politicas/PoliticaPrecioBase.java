package com.adaucu.demo.precios.politicas;

import com.adaucu.demo.precios.ContextoDeCompra;
import com.adaucu.demo.precios.Dinero;
import com.adaucu.demo.precios.PoliticaDePrecio;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class PoliticaPrecioBase implements PoliticaDePrecio {

    @Override
    public String nombre() {
        return "precio-base";
    }

    @Override
    public String descripcion() {
        return "Suma el precio base de cada asiento, sin descuentos ni recargos.";
    }

    @Override
    public Dinero calcular(ContextoDeCompra contexto) {
        BigDecimal total = contexto.asientos().stream()
                .map(item -> item.precioBase())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return Dinero.de(total);
    }
}
