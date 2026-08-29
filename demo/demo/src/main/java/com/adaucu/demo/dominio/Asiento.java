package com.adaucu.demo.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * marcaTiempo es el token de la tactica de deteccion de estados desprotegidos:
 * toda escritura sobre un asiento debe presentar la marca que leyo, y se
 * descarta si ya no coincide con la persistida (ver AsientoRepositorio).
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"partido_id", "sector", "fila", "numero"}))
public class Asiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "partido_id")
    private Partido partido;

    private String sector;

    private String fila;

    private int numero;

    @Enumerated(EnumType.STRING)
    private EstadoAsiento estado;

    private BigDecimal precioBase;

    @Column(nullable = false)
    private Instant marcaTiempo;

    protected Asiento() {
    }

    public Asiento(Partido partido, String sector, String fila, int numero, BigDecimal precioBase, Instant marcaTiempo) {
        this.partido = partido;
        this.sector = sector;
        this.fila = fila;
        this.numero = numero;
        this.estado = EstadoAsiento.LIBRE;
        this.precioBase = precioBase;
        this.marcaTiempo = marcaTiempo;
    }

    public Long getId() {
        return id;
    }

    public Partido getPartido() {
        return partido;
    }

    public String getSector() {
        return sector;
    }

    public String getFila() {
        return fila;
    }

    public int getNumero() {
        return numero;
    }

    public EstadoAsiento getEstado() {
        return estado;
    }

    public BigDecimal getPrecioBase() {
        return precioBase;
    }

    public Instant getMarcaTiempo() {
        return marcaTiempo;
    }
}
