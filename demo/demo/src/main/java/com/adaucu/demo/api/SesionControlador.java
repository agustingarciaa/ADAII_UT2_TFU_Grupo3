package com.adaucu.demo.api;

import com.adaucu.demo.api.dto.LoginPeticion;
import com.adaucu.demo.api.dto.SesionRespuesta;
import com.adaucu.demo.dominio.Sesion;
import com.adaucu.demo.seguridad.ServicioDeAutenticacion;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/v1/sesiones")
public class SesionControlador {

    private static final String PREFIJO_BEARER = "Bearer ";

    private final ServicioDeAutenticacion servicioDeAutenticacion;

    public SesionControlador(ServicioDeAutenticacion servicioDeAutenticacion) {
        this.servicioDeAutenticacion = servicioDeAutenticacion;
    }

    @PostMapping
    public SesionRespuesta iniciarSesion(@Valid @RequestBody LoginPeticion peticion) {
        Sesion sesion = servicioDeAutenticacion.iniciarSesion(peticion.email(), peticion.contrasena());
        return SesionRespuesta.desde(sesion);
    }

    @DeleteMapping("/actual")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cerrarSesion(HttpServletRequest request) {
        String encabezado = request.getHeader("Authorization");
        if (encabezado != null && encabezado.startsWith(PREFIJO_BEARER)) {
            servicioDeAutenticacion.cerrarSesion(encabezado.substring(PREFIJO_BEARER.length()).trim());
        }
    }
}
