package com.adaucu.demo.api.dto;

import java.math.BigDecimal;

public record CotizacionRespuesta(BigDecimal monto, String moneda, String politicaUsada) {
}
