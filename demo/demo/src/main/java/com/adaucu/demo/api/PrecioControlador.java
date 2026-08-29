package com.adaucu.demo.api;

import com.adaucu.demo.api.dto.CotizacionPeticion;
import com.adaucu.demo.api.dto.CotizacionRespuesta;
import com.adaucu.demo.api.dto.PoliticaRespuesta;
import com.adaucu.demo.dominio.Asiento;
import com.adaucu.demo.dominio.Partido;
import com.adaucu.demo.dominio.RolUsuario;
import com.adaucu.demo.precios.Dinero;
import com.adaucu.demo.precios.RegistroDePoliticas;
import com.adaucu.demo.repositorio.AsientoRepositorio;
import com.adaucu.demo.repositorio.PartidoRepositorio;
import com.adaucu.demo.seguridad.UsuarioActual;
import com.adaucu.demo.ventas.MotorDeVentas;
import jakarta.validation.Valid;
import java.time.Clock;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class PrecioControlador {

    private final RegistroDePoliticas registroDePoliticas;
    private final MotorDeVentas motorDeVentas;
    private final PartidoRepositorio partidoRepositorio;
    private final AsientoRepositorio asientoRepositorio;
    private final UsuarioActual usuarioActual;
    private final Clock reloj;

    public PrecioControlador(RegistroDePoliticas registroDePoliticas,
                              MotorDeVentas motorDeVentas,
                              PartidoRepositorio partidoRepositorio,
                              AsientoRepositorio asientoRepositorio,
                              UsuarioActual usuarioActual,
                              Clock reloj) {
        this.registroDePoliticas = registroDePoliticas;
        this.motorDeVentas = motorDeVentas;
        this.partidoRepositorio = partidoRepositorio;
        this.asientoRepositorio = asientoRepositorio;
        this.usuarioActual = usuarioActual;
        this.reloj = reloj;
    }

    @GetMapping("/politicas-precio")
    public List<PoliticaRespuesta> listarPoliticas() {
        return registroDePoliticas.disponibles().stream()
                .map(PoliticaRespuesta::desde)
                .toList();
    }

    @PostMapping("/cotizaciones")
    public CotizacionRespuesta cotizar(@Valid @RequestBody CotizacionPeticion peticion) {
        Partido partido = partidoRepositorio.findById(peticion.partidoId())
                .orElseThrow(() -> new EntidadNoEncontradaException("No existe el partido " + peticion.partidoId()));

        List<Asiento> asientos = asientoRepositorio.findAllById(peticion.asientoIds());
        if (asientos.size() != peticion.asientoIds().size()) {
            throw new EntidadNoEncontradaException("Uno o mas de los asientos indicados no existe");
        }

        RolUsuario rol = usuarioActual.estaAutenticado() ? usuarioActual.obtener().getRol() : RolUsuario.HINCHA;
        String nombrePolitica = motorDeVentas.resolverNombrePolitica(peticion.politica());
        Dinero monto = motorDeVentas.cotizar(partido, asientos, rol, peticion.politica(), reloj.instant());

        return new CotizacionRespuesta(monto.monto(), monto.moneda(), nombrePolitica);
    }
}
