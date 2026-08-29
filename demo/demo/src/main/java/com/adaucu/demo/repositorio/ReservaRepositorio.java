package com.adaucu.demo.repositorio;

import com.adaucu.demo.dominio.Reserva;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservaRepositorio extends JpaRepository<Reserva, Long> {

    /**
     * Cuenta las reservas de un usuario que siguen "activas": PENDIENTES y
     * todavia no vencidas. Es el numero que RNF-05 limita.
     */
    @Query("""
            select count(r) from Reserva r
             where r.usuario.id = :usuarioId
               and r.estado = com.adaucu.demo.dominio.EstadoReserva.PENDIENTE
               and r.expiraEn > :ahora""")
    long contarActivasPorUsuario(@Param("usuarioId") Long usuarioId, @Param("ahora") Instant ahora);

    List<Reserva> findByUsuarioIdOrderByCreadaEnDesc(Long usuarioId);
}
