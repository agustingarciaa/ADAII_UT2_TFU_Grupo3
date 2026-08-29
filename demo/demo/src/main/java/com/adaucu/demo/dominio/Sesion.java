package com.adaucu.demo.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.Instant;

@Entity
public class Sesion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String token;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    private Instant creadaEn;

    private Instant expiraEn;

    protected Sesion() {
    }

    public Sesion(String token, Usuario usuario, Instant creadaEn, Instant expiraEn) {
        this.token = token;
        this.usuario = usuario;
        this.creadaEn = creadaEn;
        this.expiraEn = expiraEn;
    }

    public Long getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Instant getCreadaEn() {
        return creadaEn;
    }

    public Instant getExpiraEn() {
        return expiraEn;
    }

    public boolean estaVencida(Instant ahora) {
        return ahora.isAfter(expiraEn);
    }
}
