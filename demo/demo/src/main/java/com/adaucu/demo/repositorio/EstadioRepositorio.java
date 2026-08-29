package com.adaucu.demo.repositorio;

import com.adaucu.demo.dominio.Estadio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstadioRepositorio extends JpaRepository<Estadio, Long> {
}
