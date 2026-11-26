package com.sant1sz.parche.repository;

import com.sant1sz.parche.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}
