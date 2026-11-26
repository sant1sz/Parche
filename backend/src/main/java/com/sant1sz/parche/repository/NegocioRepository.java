package com.sant1sz.parche.repository;

import com.sant1sz.parche.model.Negocio;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NegocioRepository extends JpaRepository<Negocio, Long> {

    Optional<Negocio> findByIdAndActivoTrue(Long id);
}
