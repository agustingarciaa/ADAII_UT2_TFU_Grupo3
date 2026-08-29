package com.adaucu.demo.dominio;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "reserva_id")
    private Reserva reserva;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    private BigDecimal montoTotal;

    private Instant pagadaEn;

    protected Compra() {
    }

    public Compra(Reserva reserva, Usuario usuario, BigDecimal montoTotal, Instant pagadaEn) {
        this.reserva = reserva;
        this.usuario = usuario;
        this.montoTotal = montoTotal;
        this.pagadaEn = pagadaEn;
    }

    public Long getId() {
        return id;
    }

    public Reserva getReserva() {
        return reserva;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public Instant getPagadaEn() {
        return pagadaEn;
    }
}
