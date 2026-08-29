package com.adaucu.demo.precios;

/**
 * Abstraccion de la que depende el motor de ventas (tactica de polimorfismo,
 * categoria "diferir el binding"). El motor solo conoce esta interfaz; la
 * ligadura con una implementacion concreta se resuelve en tiempo de ejecucion
 * por nombre, contra RegistroDePoliticas. Agregar una politica nueva es
 * escribir una clase que implemente esta interfaz y registrarla como
 * @Component: ningun modulo existente se modifica ni se recompila (RNF-03).
 */
public interface PoliticaDePrecio {

    String nombre();

    String descripcion();

    Dinero calcular(ContextoDeCompra contexto);
}
