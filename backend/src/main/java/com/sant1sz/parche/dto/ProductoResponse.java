package com.sant1sz.parche.dto;

import com.sant1sz.parche.model.Producto;

/** Producto del menú. {@code precio} va en pesos colombianos, sin decimales. */
public record ProductoResponse(Long id, String nombre, String descripcion, long precio) {

    public static ProductoResponse de(Producto producto) {
        return new ProductoResponse(
                producto.getId(), producto.getNombre(), producto.getDescripcion(), producto.getPrecio());
    }
}
