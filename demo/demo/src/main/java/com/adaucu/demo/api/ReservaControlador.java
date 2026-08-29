package com.adaucu.demo.api;

import com.adaucu.demo.api.dto.ReservaPeticion;
import com.adaucu.demo.api.dto.ReservaRespuesta;
import com.adaucu.demo.dominio.Reserva;
import com.adaucu.demo.dominio.Usuario;
import com.adaucu.demo.repositorio.ReservaRepositorio;
import com.adaucu.demo.seguridad.UsuarioActual;
import com.adaucu.demo.ventas.PeticionDeAsiento;
import com.adaucu.demo.ventas.ServicioDeReservas;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reservas")
public class ReservaControlador {

    private final ServicioDeReservas servicioDeReservas;
    private final ReservaRepositorio reservaRepositorio;
    private final UsuarioActual usuarioActual;

    public ReservaControlador(ServicioDeReservas servicioDeReservas,
                               ReservaRepositorio reservaRepositorio,
                               UsuarioActual usuarioActual) {
        this.servicioDeReservas = servicioDeReservas;
        this.reservaRepositorio = reservaRepositorio;
        this.usuarioActual = usuarioActual;
    }

    @PostMapping
    public ResponseEntity<ReservaRespuesta> crear(@Valid @RequestBody ReservaPeticion peticion) {
        Usuario usuario = usuarioActual.obtener();

        List<PeticionDeAsiento> pedidos = peticion.asientos().stream()
                .map(item -> new PeticionDeAsiento(item.asientoId(), item.marcaTiempo()))
                .toList();

        Reserva reserva = servicioDeReservas.crear(usuario, peticion.partidoId(), pedidos, peticion.politica());
        return ResponseEntity.status(HttpStatus.CREATED).body(ReservaRespuesta.desde(reserva));
    }

    @GetMapping("/{id}")
    public ReservaRespuesta obtener(@PathVariable Long id) {
        return ReservaRespuesta.desde(buscarPropia(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelar(@PathVariable Long id) {
        Reserva reserva = buscarPropia(id);
        servicioDeReservas.cancelar(reserva);
    }

    private Reserva buscarPropia(Long id) {
        Usuario usuario = usuarioActual.obtener();
        Reserva reserva = reservaRepositorio.findById(id)
                .orElseThrow(() -> new EntidadNoEncontradaException("No existe la reserva " + id));
        if (!reserva.getUsuario().getId().equals(usuario.getId())) {
            throw new AccesoDenegadoException("La reserva no pertenece al usuario autenticado");
        }
        return reserva;
    }
}
