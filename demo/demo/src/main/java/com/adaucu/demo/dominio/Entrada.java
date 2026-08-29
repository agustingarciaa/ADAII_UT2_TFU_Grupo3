package com.adaucu.demo.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.Instant;

/**
 * El UNIQUE sobre asiento es la ultima linea de defensa del RNF-02: por mas
 * que la logica de aplicacion fallara, la base de datos no permite emitir dos
 * entradas para el mismo asiento.
 */
@Entity
public class Entrada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "compra_id")
    private Compra compra;

    @ManyToOne
    @JoinColumn(name = "asiento_id", unique = true)
    private Asiento asiento;

    @ManyToOne
    @JoinColumn(name = "partido_id")
    private Partido partido;

    @Column(unique = true, nullable = false)
    private String codigo;

    private Instant emitidaEn;

    protected Entrada() {
    }

    public Entrada(Compra compra, Asiento asiento, Partido partido, String codigo, Instant emitidaEn) {
        this.compra = compra;
        this.asiento = asiento;
        this.partido = partido;
        this.codigo = codigo;
        this.emitidaEn = emitidaEn;
    }

    public Long getId() {
        return id;
    }

    public Compra getCompra() {
        return compra;
    }

    public Asiento getAsiento() {
        return asiento;
    }

    public Partido getPartido() {
        return partido;
    }

    public String getCodigo() {
        return codigo;
    }

    public Instant getEmitidaEn() {
        return emitidaEn;
    }
}
