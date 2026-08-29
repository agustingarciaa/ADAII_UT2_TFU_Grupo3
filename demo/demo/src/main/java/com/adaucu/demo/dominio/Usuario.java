package com.adaucu.demo.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String hashContrasena;

    @Column(nullable = false)
    private String salt;

    private String nombre;

    @Enumerated(EnumType.STRING)
    private RolUsuario rol;

    protected Usuario() {
    }

    public Usuario(String email, String hashContrasena, String salt, String nombre, RolUsuario rol) {
        this.email = email;
        this.hashContrasena = hashContrasena;
        this.salt = salt;
        this.nombre = nombre;
        this.rol = rol;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getHashContrasena() {
        return hashContrasena;
    }

    public String getSalt() {
        return salt;
    }

    public String getNombre() {
        return nombre;
    }

    public RolUsuario getRol() {
        return rol;
    }
}
