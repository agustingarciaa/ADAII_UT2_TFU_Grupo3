package com.adaucu.demo.ventas;

import java.time.Instant;

/**
 * Lo que el cliente debe enviar por cada asiento que quiere reservar: el id
 * del asiento y la marcaTiempo que observo al listar los asientos
 * disponibles. Es el "token" de la tactica de deteccion de estados
 * desprotegidos viajando de vuelta desde el cliente.
 */
public record PeticionDeAsiento(Long asientoId, Instant marcaTiempo) {
}
