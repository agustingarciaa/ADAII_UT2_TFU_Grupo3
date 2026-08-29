package com.adaucu.demo.precios;

import com.adaucu.demo.dominio.Partido;
import com.adaucu.demo.dominio.RolUsuario;
import java.time.Instant;
import java.util.List;

/**
 * Todo lo que una PoliticaDePrecio pueda llegar a necesitar viaja en este
 * contexto, para que agregar una politica nueva no obligue a cambiar la firma
 * de la abstraccion PoliticaDePrecio.
 */
public record ContextoDeCompra(
        Partido partido,
        List<ItemDeAsiento> asientos,
        RolUsuario rolUsuario,
        long entradasVendidasDelPartido,
        long capacidadDelEstadio,
        Instant momento) {
}
