package com.sant1sz.parche.service;

import com.sant1sz.parche.dto.CategoriaResponse;
import com.sant1sz.parche.dto.MenuResponse;
import com.sant1sz.parche.dto.NegocioResumenResponse;
import com.sant1sz.parche.dto.ProductoResponse;
import com.sant1sz.parche.exception.RecursoNoEncontradoException;
import com.sant1sz.parche.model.Categoria;
import com.sant1sz.parche.model.Negocio;
import com.sant1sz.parche.model.Producto;
import com.sant1sz.parche.repository.CategoriaRepository;
import com.sant1sz.parche.repository.NegocioRepository;
import com.sant1sz.parche.repository.ProductoRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MenuService {

    private final NegocioRepository negocioRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    public MenuService(
            NegocioRepository negocioRepository,
            CategoriaRepository categoriaRepository,
            ProductoRepository productoRepository) {
        this.negocioRepository = negocioRepository;
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
    }

    /**
     * Devuelve las categorías activas del negocio, en orden, con sus productos disponibles.
     * Las categorías sin productos disponibles no aparecen. Hace tres consultas, sin importar
     * el tamaño del menú, y todas filtran por el mismo negocio.
     */
    @Transactional(readOnly = true)
    public MenuResponse obtenerMenu(Long idNegocio) {
        Negocio negocio = negocioRepository.findByIdAndActivoTrue(idNegocio)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe un negocio activo con id " + idNegocio + "."));

        Map<Long, List<ProductoResponse>> productosPorCategoria = productoRepository
                .findByIdNegocioAndDisponibleTrueOrderByOrdenAscIdAsc(idNegocio)
                .stream()
                .collect(Collectors.groupingBy(
                        Producto::getIdCategoria,
                        Collectors.mapping(ProductoResponse::de, Collectors.toList())));

        List<CategoriaResponse> categorias = categoriaRepository
                .findByIdNegocioAndActivaTrueOrderByOrdenAscIdAsc(idNegocio)
                .stream()
                .filter(categoria -> productosPorCategoria.containsKey(categoria.getId()))
                .map(categoria -> aRespuesta(categoria, productosPorCategoria.get(categoria.getId())))
                .toList();

        return new MenuResponse(NegocioResumenResponse.de(negocio), categorias);
    }

    private static CategoriaResponse aRespuesta(Categoria categoria, List<ProductoResponse> productos) {
        return new CategoriaResponse(categoria.getId(), categoria.getNombre(), List.copyOf(productos));
    }
}
