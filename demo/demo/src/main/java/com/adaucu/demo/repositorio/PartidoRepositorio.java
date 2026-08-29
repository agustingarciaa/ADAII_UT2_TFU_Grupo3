package com.adaucu.demo.repositorio;

import com.adaucu.demo.dominio.Partido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartidoRepositorio extends JpaRepository<Partido, Long> {
}
