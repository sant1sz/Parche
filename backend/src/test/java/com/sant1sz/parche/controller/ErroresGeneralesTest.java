package com.sant1sz.parche.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sant1sz.parche.IntegracionTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
    @DisplayName("un método no soportado responde 405 en español")
    void metodoNoPermitido() throws Exception {
        mockMvc.perform(post("/api/negocios/1/menu"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.title").value("Método no permitido"))
                .andExpect(jsonPath("$.codigo").value("METODO_NO_PERMITIDO"));
    }
}
