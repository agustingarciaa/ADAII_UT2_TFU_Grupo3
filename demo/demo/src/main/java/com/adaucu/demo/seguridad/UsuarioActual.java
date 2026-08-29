package com.adaucu.demo.seguridad;

import com.adaucu.demo.dominio.Usuario;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

/**
 * Holder con scope de request: InterceptorDeAutenticacion lo completa al
 * resolver el token Bearer, y los controladores lo consultan para saber quien
 * hizo el pedido.
 */
@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = org.springframework.context.annotation.ScopedProxyMode.TARGET_CLASS)
public class UsuarioActual {

    private Usuario usuario;

    public Usuario obtener() {
        if (usuario == null) {
            throw new NoAutenticadoException("Esta operacion requiere autenticacion");
        }
        return usuario;
    }

    public void establecer(Usuario usuario) {
        this.usuario = usuario;
    }

    public boolean estaAutenticado() {
        return usuario != null;
    }
}
