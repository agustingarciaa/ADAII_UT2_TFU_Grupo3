package com.adaucu.demo.repositorio;

import com.adaucu.demo.dominio.Entrada;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EntradaRepositorio extends JpaRepository<Entrada, Long> {

    List<Entrada> findByCompraUsuarioIdOrderByEmitidaEnDesc(Long usuarioId);

    long countByAsientoPartidoId(Long partidoId);
}
