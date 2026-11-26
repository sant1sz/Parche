package com.sant1sz.parche.controller;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sant1sz.parche.IntegracionTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

/** Los errores que genera Spring también salen con el cuerpo uniforme y en español. */
@DisplayName("Cuerpo de error uniforme")
class ErroresGeneralesTest extends IntegracionTest {

    @Test
    @DisplayName("una ruta inexistente responde 404 en español")
    void rutaInexistente() throws Exception {
        mockMvc.perform(get("/api/no-existe"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Recurso no encontrado"))
                .andExpect(jsonPath("$.detail").value("No encontramos lo que buscas."))
                .andExpect(jsonPath("$.codigo").value("RECURSO_NO_ENCONTRADO"))
                .andExpect(jsonPath("$.instance").value("/api/no-existe"))
                .andExpect(jsonPath("$.marcaTiempo").isNotEmpty());
    }

    @Test
    @DisplayName("con restricción en la ruta y cuerpo @Valid, los errores del cuerpo nombran el campo")
    void erroresDelCuerpoNombranElCampo() throws Exception {
        mockMvc.perform(put("/api/pruebas-errores/{id}", 0)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\": \"\", \"precio\": -1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACION"))
                .andExpect(jsonPath("$.errores[*].campo").value(containsInAnyOrder("id", "nombre", "precio")))
                .andExpect(jsonPath("$.errores[?(@.campo == 'nombre')].mensaje").value(contains("El nombre es obligatorio.")))
                .andExpect(jsonPath("$.errores[?(@.campo == 'precio')].mensaje")
                        .value(contains("El precio no puede ser negativo.")));
    }

    @Test
    @DisplayName("una excepción con @ResponseStatus responde con su estado y su razón")
    void respetaResponseStatusConRazon() throws Exception {
        mockMvc.perform(get("/api/pruebas-errores/conflicto"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.title").value("Conflicto"))
                .andExpect(jsonPath("$.detail").value("Ya existe un producto con ese nombre."))
                .andExpect(jsonPath("$.codigo").value("CONFLICTO"));
    }

    @Test
    @DisplayName("una excepción con @ResponseStatus sin razón usa el texto genérico del estado")
    void respetaResponseStatusSinRazon() throws Exception {
        mockMvc.perform(get("/api/pruebas-errores/conflicto-sin-razon"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(
                        "La operación entra en conflicto con el estado actual de los datos."));
    }

    @Test
    @DisplayName("una excepción inesperada responde 500 sin exponer detalles internos")
    void errorInesperado() throws Exception {
        mockMvc.perform(get("/api/pruebas-errores/inesperado"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.codigo").value("ERROR_INTERNO"))
                .andExpect(jsonPath("$.detail").value("Ocurrió un error inesperado. Intenta de nuevo en un momento."));
    }

    @Test
    @DisplayName("un método no soportado responde 405 en español")
    void metodoNoPermitido() throws Exception {
        mockMvc.perform(post("/api/negocios/1/menu"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.title").value("Método no permitido"))
                .andExpect(jsonPath("$.codigo").value("METODO_NO_PERMITIDO"));
    }
}
