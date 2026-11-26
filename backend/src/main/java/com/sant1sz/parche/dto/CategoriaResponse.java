package com.sant1sz.parche.dto;

import java.util.List;

public record CategoriaResponse(Long id, String nombre, List<ProductoResponse> productos) {
}
