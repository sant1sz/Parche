package com.sant1sz.parche.dto;

import java.util.List;

/** Menú de un negocio: sus categorías activas, en orden, con los productos disponibles. */
public record MenuResponse(NegocioResumenResponse negocio, List<CategoriaResponse> categorias) {
}
