package com.adaucu.demo.repositorio;

import com.adaucu.demo.dominio.Sesion;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SesionRepositorio extends JpaRepository<Sesion, Long> {

    Optional<Sesion> findByToken(String token);

    void deleteByToken(String token);
}
