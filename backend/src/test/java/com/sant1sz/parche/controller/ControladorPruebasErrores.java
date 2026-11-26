package com.sant1sz.parche.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador que solo existe en las pruebas (src/test). Sirve para ejercitar casos del
 * manejo de errores que la API real todavía no tiene.
 */
@RestController
@RequestMapping("/api/pruebas-errores")
class ControladorPruebasErrores {

    record SolicitudPrueba(
            @NotBlank(message = "El nombre es obligatorio.") String nombre,
            @PositiveOrZero(message = "El precio no puede ser negativo.") long precio) {
    }

    @ResponseStatus(value = HttpStatus.CONFLICT, reason = "Ya existe un producto con ese nombre.")
    static class ConflictoPrueba extends RuntimeException {
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    static class ConflictoSinRazon extends RuntimeException {
    }

    /** Combina una restricción en la ruta con un cuerpo @Valid: Spring usa validación de método. */
    @PutMapping("/{id}")
    SolicitudPrueba actualizar(
            @PathVariable @Positive(message = "Debe ser un número mayor que cero.") Long id,
            @Valid @RequestBody SolicitudPrueba solicitud) {
        return solicitud;
    }

    @GetMapping("/conflicto")
    void conflicto() {
        throw new ConflictoPrueba();
    }

    @GetMapping("/conflicto-sin-razon")
    void conflictoSinRazon() {
        throw new ConflictoSinRazon();
    }

    @GetMapping("/inesperado")
    void inesperado() {
        throw new IllegalStateException("detalle interno que no debe llegar al cliente");
    }
}
