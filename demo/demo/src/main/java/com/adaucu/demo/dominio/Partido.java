package com.adaucu.demo.dominio;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.Instant;

@Entity
public class Partido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String equipoLocal;

    private String equipoVisitante;

    private Instant comienzaEn;

    @ManyToOne
    @JoinColumn(name = "estadio_id")
    private Estadio estadio;

    @Enumerated(EnumType.STRING)
    private EstadoPartido estado;

    protected Partido() {
    }

    public Partido(String equipoLocal, String equipoVisitante, Instant comienzaEn, Estadio estadio) {
        this.equipoLocal = equipoLocal;
        this.equipoVisitante = equipoVisitante;
        this.comienzaEn = comienzaEn;
        this.estadio = estadio;
        this.estado = EstadoPartido.PROGRAMADO;
    }

    public Long getId() {
        return id;
    }

    public String getEquipoLocal() {
        return equipoLocal;
    }

    public String getEquipoVisitante() {
        return equipoVisitante;
    }

    public Instant getComienzaEn() {
        return comienzaEn;
    }

    public Estadio getEstadio() {
        return estadio;
    }

    public EstadoPartido getEstado() {
        return estado;
    }
}
