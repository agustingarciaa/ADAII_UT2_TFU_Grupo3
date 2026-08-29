package com.adaucu.demo.api;

import com.adaucu.demo.api.dto.CompraRespuesta;
import com.adaucu.demo.api.dto.EntradaRespuesta;
import com.adaucu.demo.dominio.Compra;
import com.adaucu.demo.dominio.Reserva;
import com.adaucu.demo.dominio.Usuario;
import com.adaucu.demo.repositorio.EntradaRepositorio;
import com.adaucu.demo.repositorio.ReservaRepositorio;
import com.adaucu.demo.seguridad.UsuarioActual;
import com.adaucu.demo.ventas.ServicioDeCompras;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class CompraControlador {

    private final ServicioDeCompras servicioDeCompras;
    private final ReservaRepositorio reservaRepositorio;
    private final EntradaRepositorio entradaRepositorio;
    private final UsuarioActual usuarioActual;

    public CompraControlador(ServicioDeCompras servicioDeCompras,
                              ReservaRepositorio reservaRepositorio,
                              EntradaRepositorio entradaRepositorio,
                              UsuarioActual usuarioActual) {
        this.servicioDeCompras = servicioDeCompras;
        this.reservaRepositorio = reservaRepositorio;
        this.entradaRepositorio = entradaRepositorio;
        this.usuarioActual = usuarioActual;
    }

    @PostMapping("/reservas/{id}/compra")
    public ResponseEntity<CompraRespuesta> confirmar(@PathVariable Long id) {
        Usuario usuario = usuarioActual.obtener();
        Reserva reserva = reservaRepositorio.findById(id)
                .orElseThrow(() -> new EntidadNoEncontradaException("No existe la reserva " + id));
        if (!reserva.getUsuario().getId().equals(usuario.getId())) {
            throw new AccesoDenegadoException("La reserva no pertenece al usuario autenticado");
        }

        Compra compra = servicioDeCompras.confirmar(reserva, usuario);
        List<EntradaRespuesta> entradas = entradaRepositorio.findByCompraUsuarioIdOrderByEmitidaEnDesc(usuario.getId())
                .stream()
                .filter(e -> e.getCompra().getId().equals(compra.getId()))
                .map(EntradaRespuesta::desde)
                .toList();

        return ResponseEntity.status(HttpStatus.CREATED).body(CompraRespuesta.desde(compra, entradas));
    }

    @GetMapping("/entradas")
    public List<EntradaRespuesta> misEntradas() {
        Usuario usuario = usuarioActual.obtener();
        return entradaRepositorio.findByCompraUsuarioIdOrderByEmitidaEnDesc(usuario.getId()).stream()
                .map(EntradaRespuesta::desde)
                .toList();
    }
}
