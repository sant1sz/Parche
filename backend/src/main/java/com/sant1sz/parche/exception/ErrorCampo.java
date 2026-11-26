package com.sant1sz.parche.exception;

/** Error de validación de un campo o parámetro concreto. */
public record ErrorCampo(String campo, String mensaje) {
}
