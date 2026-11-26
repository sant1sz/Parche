package com.sant1sz.parche.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Producto del menú. El precio está en pesos colombianos, como entero.
 * La base de datos garantiza que la categoría pertenezca al mismo negocio.
 */
@Entity
@Table(name = "producto")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_negocio", nullable = false)
    private Long idNegocio;

    @Column(name = "id_categoria", nullable = false)
    private Long idCategoria;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(length = 300)
    private String descripcion;

    @Column(nullable = false)
    private long precio;

    @Column(nullable = false)
    private boolean disponible = true;

    @Column(nullable = false)
    private int orden;

    protected Producto() {
    }

    public Long getId() {
        return id;
    }

    public Long getIdNegocio() {
        return idNegocio;
    }

    public Long getIdCategoria() {
        return idCategoria;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public long getPrecio() {
        return precio;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public int getOrden() {
        return orden;
    }
}
