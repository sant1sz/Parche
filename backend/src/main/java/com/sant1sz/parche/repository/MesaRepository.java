package com.sant1sz.parche.repository;

import com.sant1sz.parche.model.Mesa;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MesaRepository extends JpaRepository<Mesa, Long> {

    /** Busca una mesa activa de un negocio activo, trayendo el negocio en la misma consulta. */
    @Query("""
            select m from Mesa m
            join fetch m.negocio n
            where m.token = :token and m.activa = true and n.activo = true
            """)
    Optional<Mesa> buscarActivaPorToken(String token);
}
