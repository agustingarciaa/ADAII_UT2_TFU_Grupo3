package com.adaucu.demo.dominio;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/**
 * No lleva UNIQUE sobre asiento_id: un mismo asiento pasa por varias reservas
 * a lo largo del tiempo (reservado, cancelado, vuelto a reservar). La
 * exclusividad de "reservado ahora mismo" la da el estado del Asiento
 * (protegido por la tactica timestamp); la exclusividad definitiva de "vendido
 * una sola vez" la da el UNIQUE(asiento_id) de Entrada.
 */
@Entity
public class ReservaAsiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "reserva_id")
    private Reserva reserva;

    @ManyToOne
    @JoinColumn(name = "asiento_id")
    private Asiento asiento;

    protected ReservaAsiento() {
    }

    public ReservaAsiento(Reserva reserva, Asiento asiento) {
        this.reserva = reserva;
        this.asiento = asiento;
    }

    public Long getId() {
        return id;
    }

    public Reserva getReserva() {
        return reserva;
    }

    public Asiento getAsiento() {
        return asiento;
    }
}
