package com.adaucu.demo.api.dto;

import com.adaucu.demo.dominio.Usuario;

public record UsuarioRespuesta(Long id, String email, String nombre, String rol) {

    public static UsuarioRespuesta desde(Usuario usuario) {
        return new UsuarioRespuesta(usuario.getId(), usuario.getEmail(), usuario.getNombre(), usuario.getRol().name());
    }
}
