package com.adaucu.demo.seguridad;

import com.adaucu.demo.dominio.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import org.springframework.web.servlet.HandlerInterceptor;

public class InterceptorDeAutenticacion implements HandlerInterceptor {

    private static final String PREFIJO_BEARER = "Bearer ";

    private final ServicioDeAutenticacion servicioDeAutenticacion;
    private final UsuarioActual usuarioActual;

    public InterceptorDeAutenticacion(ServicioDeAutenticacion servicioDeAutenticacion, UsuarioActual usuarioActual) {
        this.servicioDeAutenticacion = servicioDeAutenticacion;
        this.usuarioActual = usuarioActual;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String encabezado = request.getHeader("Authorization");
        if (encabezado != null && encabezado.startsWith(PREFIJO_BEARER)) {
            String token = encabezado.substring(PREFIJO_BEARER.length()).trim();
            Optional<Usuario> usuario = servicioDeAutenticacion.resolverUsuarioPorToken(token);
            usuario.ifPresent(usuarioActual::establecer);
        }
        return true;
    }
}
