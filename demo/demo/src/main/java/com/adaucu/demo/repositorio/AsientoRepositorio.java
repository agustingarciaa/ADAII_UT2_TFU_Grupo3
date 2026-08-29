package com.adaucu.demo.repositorio;

import com.adaucu.demo.dominio.Asiento;
import com.adaucu.demo.dominio.EstadoAsiento;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AsientoRepositorio extends JpaRepository<Asiento, Long> {

    List<Asiento> findByPartidoIdOrderBySectorAscFilaAscNumeroAsc(Long partidoId);

    long countByPartidoId(Long partidoId);

    /**
     * Escritura condicionada a la tactica de timestamp: solo mueve el asiento
     * si su marcaTiempo actual coincide exactamente con la que el cliente leyo
     * antes de pedir la reserva. Si otra transaccion ya modifico el asiento en
     * el medio, la marca ya no coincide y esta consulta actualiza 0 filas -
     * esa es la senal de conflicto que usa ServicioDeReservas.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Asiento a
               set a.estado = :nuevoEstado, a.marcaTiempo = :nuevaMarca
             where a.id = :id and a.marcaTiempo = :marcaEsperada""")
    int actualizarEstadoSiLaMarcaCoincide(@Param("id") Long id,
                                           @Param("marcaEsperada") Instant marcaEsperada,
                                           @Param("nuevoEstado") EstadoAsiento nuevoEstado,
                                           @Param("nuevaMarca") Instant nuevaMarca);
}
