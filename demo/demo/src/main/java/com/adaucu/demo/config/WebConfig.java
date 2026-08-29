package com.adaucu.demo.config;

import com.adaucu.demo.seguridad.InterceptorDeAutenticacion;
import com.adaucu.demo.seguridad.ServicioDeAutenticacion;
import com.adaucu.demo.seguridad.UsuarioActual;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * El interceptor solo resuelve el token Bearer, si viene, en UsuarioActual;
 * no bloquea rutas. Cada endpoint que requiere sesion llama
 * UsuarioActual.obtener(), que lanza NoAutenticadoException (401) si nadie se
 * autentico - asi no hace falta mantener una lista de rutas publicas aparte.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final ServicioDeAutenticacion servicioDeAutenticacion;
    private final UsuarioActual usuarioActual;

    public WebConfig(ServicioDeAutenticacion servicioDeAutenticacion, UsuarioActual usuarioActual) {
        this.servicioDeAutenticacion = servicioDeAutenticacion;
        this.usuarioActual = usuarioActual;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new InterceptorDeAutenticacion(servicioDeAutenticacion, usuarioActual));
    }
}
