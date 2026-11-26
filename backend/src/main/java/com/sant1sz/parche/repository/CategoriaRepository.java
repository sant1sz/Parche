package com.sant1sz.parche.repository;

import com.sant1sz.parche.model.Categoria;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    List<Categoria> findByIdNegocioAndActivaTrueOrderByOrdenAscIdAsc(Long idNegocio);
}
