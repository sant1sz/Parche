package com.sant1sz.parche.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sant1sz.parche.IntegracionTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

@DisplayName("GET /api/mesas/{token}")
class MesaControllerTest extends IntegracionTest {

    /** Tokens fijos de Café La Ceiba (db/demo/R__demo_cafe_la_ceiba.sql). */
    private static final String TOKEN_MESA_1 = "hGo0Aj_ObuejR_d4UPJumA";
    private static final String TOKEN_MESA_12 = "V4hNwvn3u1x6iIgcBNv-FA";

    @Test
    @DisplayName("devuelve el negocio y el número de la mesa")
    void devuelveMesa() throws Exception {
        mockMvc.perform(get("/api/mesas/{token}", TOKEN_MESA_1))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.numero").value(1))
                .andExpect(jsonPath("$.negocio.id").value(idCafeLaCeiba()))
                .andExpect(jsonPath("$.negocio.nombre").value("Café La Ceiba"))
                .andExpect(jsonPath("$.negocio.demo").value(true))
                .andExpect(jsonPath("$.token").doesNotExist());

        mockMvc.perform(get("/api/mesas/{token}", TOKEN_MESA_12))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numero").value(12));
    }

    @Test
    @DisplayName("responde 404 si el token no existe, sin repetir el token en el mensaje")
    void tokenInexistente() throws Exception {
        mockMvc.perform(get("/api/mesas/{token}", "TokenQueNoExiste123"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.codigo").value("RECURSO_NO_ENCONTRADO"))
                .andExpect(jsonPath("$.detail").value("No encontramos esta mesa. Pide ayuda al personal del lugar."));
    }

    @Test
    @DisplayName("responde 404 si la mesa está inactiva")
    void mesaInactiva() throws Exception {
        jdbc.update("update mesa set activa = false where token = ?", TOKEN_MESA_1);

        mockMvc.perform(get("/api/mesas/{token}", TOKEN_MESA_1))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("responde 404 si el negocio de la mesa está inactivo")
    void negocioInactivo() throws Exception {
        jdbc.update("update negocio set activo = false where id = ?", idCafeLaCeiba());

        mockMvc.perform(get("/api/mesas/{token}", TOKEN_MESA_1))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("responde 400 si el token no tiene un formato válido")
    void tokenConFormatoInvalido() throws Exception {
        mockMvc.perform(get("/api/mesas/{token}", "corto"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACION"))
                .andExpect(jsonPath("$.errores[0].campo").value("token"))
                .andExpect(jsonPath("$.errores[0].mensaje").value("El código de la mesa no tiene un formato válido."));
    }
}
