package com.adaucu.demo.dominio;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "partido_id")
    private Partido partido;

    @Enumerated(EnumType.STRING)
    private EstadoReserva estado;

    private Instant creadaEn;

    private Instant expiraEn;

    private String politicaAplicada;

    private BigDecimal montoTotal;

    @OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ReservaAsiento> asientos = new ArrayList<>();

    protected Reserva() {
    }

    public Reserva(Usuario usuario, Partido partido, Instant creadaEn, Instant expiraEn) {
        this.usuario = usuario;
        this.partido = partido;
        this.estado = EstadoReserva.PENDIENTE;
        this.creadaEn = creadaEn;
        this.expiraEn = expiraEn;
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Partido getPartido() {
        return partido;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public void setEstado(EstadoReserva estado) {
        this.estado = estado;
    }

    public Instant getCreadaEn() {
        return creadaEn;
    }

    public Instant getExpiraEn() {
        return expiraEn;
    }

    public String getPoliticaAplicada() {
        return politicaAplicada;
    }

    public void setPoliticaAplicada(String politicaAplicada) {
        this.politicaAplicada = politicaAplicada;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(BigDecimal montoTotal) {
        this.montoTotal = montoTotal;
    }

    public List<ReservaAsiento> getAsientos() {
        return asientos;
    }

    public void agregarAsiento(Asiento asiento) {
        asientos.add(new ReservaAsiento(this, asiento));
    }

    public boolean estaVencida(Instant ahora) {
        return ahora.isAfter(expiraEn);
    }
}
