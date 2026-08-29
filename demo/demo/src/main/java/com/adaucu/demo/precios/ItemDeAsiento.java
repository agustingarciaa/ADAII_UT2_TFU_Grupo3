package com.adaucu.demo.precios;

import java.math.BigDecimal;

public record ItemDeAsiento(Long asientoId, String sector, BigDecimal precioBase) {
}
