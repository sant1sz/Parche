package com.sant1sz.parche.dto;

import com.sant1sz.parche.model.Negocio;

/**
 * Datos públicos de un negocio.
 *
 * @param demo {@code true} si el negocio es simulado; la interfaz debe mostrar un aviso de "Demo".
 */
public record NegocioResumenResponse(Long id, String nombre, String barrio, String ciudad, boolean demo) {

    public static NegocioResumenResponse de(Negocio negocio) {
        return new NegocioResumenResponse(
                negocio.getId(), negocio.getNombre(), negocio.getBarrio(), negocio.getCiudad(), negocio.isDemo());
    }
}
