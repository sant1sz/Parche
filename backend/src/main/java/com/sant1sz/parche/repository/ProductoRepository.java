package com.sant1sz.parche.repository;

import com.sant1sz.parche.model.Producto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByIdNegocioAndDisponibleTrueOrderByOrdenAscIdAsc(Long idNegocio);
}
