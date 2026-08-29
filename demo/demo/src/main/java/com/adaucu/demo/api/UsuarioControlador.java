package com.adaucu.demo.api;

import com.adaucu.demo.api.dto.RegistroUsuarioPeticion;
import com.adaucu.demo.api.dto.UsuarioRespuesta;
import com.adaucu.demo.dominio.Usuario;
import com.adaucu.demo.seguridad.ServicioDeAutenticacion;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioControlador {

    private final ServicioDeAutenticacion servicioDeAutenticacion;

    public UsuarioControlador(ServicioDeAutenticacion servicioDeAutenticacion) {
        this.servicioDeAutenticacion = servicioDeAutenticacion;
    }

    @PostMapping
    public ResponseEntity<UsuarioRespuesta> registrar(@Valid @RequestBody RegistroUsuarioPeticion peticion) {
        Usuario usuario = servicioDeAutenticacion.registrar(peticion.email(), peticion.contrasena(), peticion.nombre());
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioRespuesta.desde(usuario));
    }
}
