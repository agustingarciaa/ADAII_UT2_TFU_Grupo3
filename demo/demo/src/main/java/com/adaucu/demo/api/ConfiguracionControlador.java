package com.adaucu.demo.api;

import com.adaucu.demo.api.dto.ConfiguracionRespuesta;
import com.adaucu.demo.config.ParametrosDeVenta;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Solo lectura: expone los parametros que realmente resolvio este proceso al
 * arrancar, para poder verificar que un cambio en config/ventas.properties
 * (o en variables de entorno) quedo activo tras reiniciar el servicio, sin
 * haber recompilado nada.
 */
@RestController
@RequestMapping("/api/v1/configuracion")
public class ConfiguracionControlador {

    private final ParametrosDeVenta parametros;

    public ConfiguracionControlador(ParametrosDeVenta parametros) {
        this.parametros = parametros;
    }

    @GetMapping
    public ConfiguracionRespuesta obtener() {
        return ConfiguracionRespuesta.desde(parametros);
    }
}
