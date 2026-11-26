package com.sant1sz.parche.dto;

/** Lo que el cliente necesita saber al escanear el QR de una mesa. */
public record MesaResponse(NegocioResumenResponse negocio, int numero) {
}
