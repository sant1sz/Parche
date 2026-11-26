package com.sant1sz.parche.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Mesa física de un negocio. Su token es lo que va en el QR o la etiqueta NFC. */
@Entity
@Table(name = "mesa")
public class Mesa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_negocio", nullable = false)
    private Negocio negocio;

    @Column(nullable = false)
    private int numero;

    @Column(nullable = false, length = 64, unique = true)
    private String token;

    @Column(nullable = false)
    private boolean activa = true;

    protected Mesa() {
    }

    public Long getId() {
        return id;
    }

    public Negocio getNegocio() {
        return negocio;
    }

    public int getNumero() {
        return numero;
    }

    public String getToken() {
        return token;
    }

    public boolean isActiva() {
        return activa;
    }
}
