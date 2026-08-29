package com.adaucu.demo.api.dto;

import com.adaucu.demo.dominio.Sesion;
import java.time.Instant;

public record SesionRespuesta(String token, Instant expiraEn) {

    public static SesionRespuesta desde(Sesion sesion) {
        return new SesionRespuesta(sesion.getToken(), sesion.getExpiraEn());
    }
}
