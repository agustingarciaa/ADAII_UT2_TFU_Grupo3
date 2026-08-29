package com.adaucu.demo.seguridad;

import com.adaucu.demo.dominio.RolUsuario;
import com.adaucu.demo.dominio.Sesion;
import com.adaucu.demo.dominio.Usuario;
import com.adaucu.demo.repositorio.SesionRepositorio;
import com.adaucu.demo.repositorio.UsuarioRepositorio;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServicioDeAutenticacion {

    private static final long HORAS_DE_VIGENCIA_DE_SESION = 12;

    private final UsuarioRepositorio usuarioRepositorio;
    private final SesionRepositorio sesionRepositorio;
    private final HashDeContrasena hashDeContrasena;
    private final Clock reloj;
    private final SecureRandom aleatorio = new SecureRandom();

    public ServicioDeAutenticacion(UsuarioRepositorio usuarioRepositorio,
                                    SesionRepositorio sesionRepositorio,
                                    HashDeContrasena hashDeContrasena,
                                    Clock reloj) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.sesionRepositorio = sesionRepositorio;
        this.hashDeContrasena = hashDeContrasena;
        this.reloj = reloj;
    }

    @Transactional
    public Usuario registrar(String email, String contrasena, String nombre) {
        if (usuarioRepositorio.existsByEmail(email)) {
            throw new IllegalArgumentException("Ya existe un usuario registrado con ese email");
        }
        String salt = hashDeContrasena.generarSalt();
        String hash = hashDeContrasena.hash(contrasena, salt);
        Usuario usuario = new Usuario(email, hash, salt, nombre, RolUsuario.HINCHA);
        return usuarioRepositorio.save(usuario);
    }

    @Transactional
    public Sesion iniciarSesion(String email, String contrasena) {
        Usuario usuario = usuarioRepositorio.findByEmail(email)
                .orElseThrow(() -> new CredencialesInvalidasException("Email o contrasena invalidos"));

        if (!hashDeContrasena.coincide(contrasena, usuario.getSalt(), usuario.getHashContrasena())) {
            throw new CredencialesInvalidasException("Email o contrasena invalidos");
        }

        String token = UUID.randomUUID() + "-" + Long.toHexString(aleatorio.nextLong());
        Instant ahora = reloj.instant();
        Instant expiraEn = ahora.plus(HORAS_DE_VIGENCIA_DE_SESION, ChronoUnit.HOURS);
        Sesion sesion = new Sesion(token, usuario, ahora, expiraEn);
        return sesionRepositorio.save(sesion);
    }

    @Transactional
    public void cerrarSesion(String token) {
        sesionRepositorio.deleteByToken(token);
    }

    public Optional<Usuario> resolverUsuarioPorToken(String token) {
        return sesionRepositorio.findByToken(token)
                .filter(sesion -> !sesion.estaVencida(reloj.instant()))
                .map(Sesion::getUsuario);
    }
}
