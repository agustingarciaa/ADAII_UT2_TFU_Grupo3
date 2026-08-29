package com.adaucu.demo.repositorio;

import com.adaucu.demo.dominio.Compra;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompraRepositorio extends JpaRepository<Compra, Long> {
}
