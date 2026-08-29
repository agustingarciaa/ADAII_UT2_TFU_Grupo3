package com.adaucu.demo.precios.politicas;

import com.adaucu.demo.precios.ContextoDeCompra;
import com.adaucu.demo.precios.Dinero;
import com.adaucu.demo.precios.PoliticaDePrecio;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

@Component
public class PoliticaDemanda implements PoliticaDePrecio {

    private static final BigDecimal UMBRAL_DEMANDA_ALTA = new BigDecimal("0.80");
    private static final BigDecimal RECARGO_DEMANDA_ALTA = new BigDecimal("0.15");

    @Override
    public String nombre() {
        return "demanda";
    }

    @Override
    public String descripcion() {
        return "Recarga un 15% el precio base cuando ya se vendio mas del 80% de la capacidad del estadio.";
    }

    @Override
    public Dinero calcular(ContextoDeCompra contexto) {
        BigDecimal total = contexto.asientos().stream()
                .map(item -> item.precioBase())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (contexto.capacidadDelEstadio() > 0) {
            BigDecimal ocupacion = BigDecimal.valueOf(contexto.entradasVendidasDelPartido())
                    .divide(BigDecimal.valueOf(contexto.capacidadDelEstadio()), 4, RoundingMode.HALF_UP);
            if (ocupacion.compareTo(UMBRAL_DEMANDA_ALTA) >= 0) {
                total = total.multiply(BigDecimal.ONE.add(RECARGO_DEMANDA_ALTA));
            }
        }
        return Dinero.de(total);
    }
}
