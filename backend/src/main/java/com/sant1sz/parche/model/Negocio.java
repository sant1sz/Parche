package com.sant1sz.parche.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Restaurante o cafetería que usa Parche. Casi todos los demás datos cuelgan de un negocio. */
@Entity
@Table(name = "negocio")
public class Negocio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, length = 60, unique = true)
    private String slug;

    @Column(length = 80)
    private String barrio;

    @Column(nullable = false, length = 80)
    private String ciudad;

    /** Indica que el negocio y sus datos son simulados. La interfaz debe mostrarlo como "Demo". */
    @Column(name = "es_demo", nullable = false)
    private boolean demo;

    @Column(nullable = false)
    private boolean activo = true;

    protected Negocio() {
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getSlug() {
        return slug;
    }

    public String getBarrio() {
        return barrio;
    }

    public String getCiudad() {
        return ciudad;
    }

    public boolean isDemo() {
        return demo;
    }

    public boolean isActivo() {
        return activo;
    }
}
