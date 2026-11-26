package com.sant1sz.parche.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Sección del menú de un negocio (por ejemplo, "Desayunos"). */
@Entity
@Table(name = "categoria")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_negocio", nullable = false)
    private Long idNegocio;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(nullable = false)
    private int orden;

    @Column(nullable = false)
    private boolean activa = true;

    protected Categoria() {
    }

    public Long getId() {
        return id;
    }

    public Long getIdNegocio() {
        return idNegocio;
    }

    public String getNombre() {
        return nombre;
    }

    public int getOrden() {
        return orden;
    }

    public boolean isActiva() {
        return activa;
    }
}
