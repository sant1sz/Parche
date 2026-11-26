package com.sant1sz.parche.exception;

/** Se lanza cuando lo que se pide no existe o no está disponible. Responde 404. */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
