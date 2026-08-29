package com.adaucu.demo.api;

import com.adaucu.demo.api.dto.AsientoRespuesta;
import com.adaucu.demo.api.dto.PartidoRespuesta;
import com.adaucu.demo.dominio.Partido;
import com.adaucu.demo.repositorio.AsientoRepositorio;
import com.adaucu.demo.repositorio.PartidoRepositorio;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/partidos")
public class PartidoControlador {

    private final PartidoRepositorio partidoRepositorio;
    private final AsientoRepositorio asientoRepositorio;

    public PartidoControlador(PartidoRepositorio partidoRepositorio, AsientoRepositorio asientoRepositorio) {
        this.partidoRepositorio = partidoRepositorio;
        this.asientoRepositorio = asientoRepositorio;
    }

    @GetMapping
    public List<PartidoRespuesta> listar() {
        return partidoRepositorio.findAll().stream()
                .map(PartidoRespuesta::desde)
                .toList();
    }

    @GetMapping("/{id}")
    public PartidoRespuesta obtener(@PathVariable Long id) {
        return PartidoRespuesta.desde(buscarPartido(id));
    }

    @GetMapping("/{id}/asientos")
    public List<AsientoRespuesta> listarAsientos(@PathVariable Long id) {
        buscarPartido(id);
        return asientoRepositorio.findByPartidoIdOrderBySectorAscFilaAscNumeroAsc(id).stream()
                .map(AsientoRespuesta::desde)
                .toList();
    }

    private Partido buscarPartido(Long id) {
        return partidoRepositorio.findById(id)
                .orElseThrow(() -> new EntidadNoEncontradaException("No existe el partido " + id));
    }
}
