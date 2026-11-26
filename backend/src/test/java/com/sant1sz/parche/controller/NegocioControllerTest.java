package com.sant1sz.parche.controller;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sant1sz.parche.IntegracionTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

@DisplayName("GET /api/negocios/{id}/menu")
class NegocioControllerTest extends IntegracionTest {

    @Nested
    @DisplayName("cuando el negocio existe")
    class Existe {

        @Test
        @DisplayName("devuelve el negocio marcado como demo y las 5 categorías en orden")
        void devuelveMenuDeCafeLaCeiba() throws Exception {
            mockMvc.perform(get("/api/negocios/{id}/menu", idCafeLaCeiba()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.negocio.nombre").value("Café La Ceiba"))
                    .andExpect(jsonPath("$.negocio.barrio").value("Laureles"))
                    .andExpect(jsonPath("$.negocio.demo").value(true))
                    .andExpect(jsonPath("$.categorias[*].nombre").value(contains(
                            "Desayunos", "Almuerzos", "Bebidas calientes", "Bebidas frías", "Postres")))
                    .andExpect(jsonPath("$.categorias[*].productos[*]").value(hasSize(29)))
                    .andExpect(jsonPath("$.categorias[0].productos[0].nombre").value("Calentado paisa"))
                    .andExpect(jsonPath("$.categorias[0].productos[0].precio").value(18000))
                    .andExpect(jsonPath("$.categorias[*].productos[*].id").value(everyItem(notNullValue())));
        }

        @Test
        @DisplayName("no incluye productos no disponibles")
        void omiteProductosNoDisponibles() throws Exception {
            mockMvc.perform(get("/api/negocios/{id}/menu", idCafeLaCeiba()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.categorias[*].productos[*].nombre").value(not(hasItem("Tamal antioqueño"))))
                    .andExpect(jsonPath("$.categorias[0].productos").value(hasSize(5)));
        }

        @Test
        @DisplayName("omite las categorías inactivas o sin productos disponibles")
        void omiteCategoriasVaciasEInactivas() throws Exception {
            long id = idCafeLaCeiba();
            jdbc.update("""
                    update producto set disponible = false
                    where id_negocio = ?
                      and id_categoria = (select id from categoria where id_negocio = ? and nombre = 'Postres')
                    """, id, id);
            jdbc.update("update categoria set activa = false where id_negocio = ? and nombre = 'Almuerzos'", id);

            mockMvc.perform(get("/api/negocios/{id}/menu", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.categorias[*].nombre").value(contains(
                            "Desayunos", "Bebidas calientes", "Bebidas frías")));
        }

        @Test
        @DisplayName("no mezcla productos de otro negocio")
        void noMezclaNegocios() throws Exception {
            Long otro = jdbc.queryForObject(
                    "insert into negocio (nombre, slug) values ('Otro negocio', 'otro-negocio') returning id",
                    Long.class);
            Long categoria = jdbc.queryForObject(
                    "insert into categoria (id_negocio, nombre) values (?, 'Desayunos') returning id",
                    Long.class, otro);
            jdbc.update(
                    "insert into producto (id_negocio, id_categoria, nombre, precio) values (?, ?, 'Producto ajeno', 1000)",
                    otro, categoria);

            mockMvc.perform(get("/api/negocios/{id}/menu", idCafeLaCeiba()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.categorias[*].productos[*].nombre").value(not(hasItem("Producto ajeno"))));

            mockMvc.perform(get("/api/negocios/{id}/menu", otro))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.negocio.demo").value(false))
                    .andExpect(jsonPath("$.categorias[*].productos[*].nombre").value(contains("Producto ajeno")));
        }
    }

    @Nested
    @DisplayName("cuando el negocio no se puede mostrar")
    class Errores {

        @Test
        @DisplayName("responde 404 con el cuerpo de error uniforme si no existe")
        void negocioInexistente() throws Exception {
            mockMvc.perform(get("/api/negocios/{id}/menu", 999_999))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.title").value("Recurso no encontrado"))
                    .andExpect(jsonPath("$.codigo").value("RECURSO_NO_ENCONTRADO"))
                    .andExpect(jsonPath("$.detail").value("No existe un negocio activo con id 999999."))
                    .andExpect(jsonPath("$.instance").value("/api/negocios/999999/menu"))
                    .andExpect(jsonPath("$.marcaTiempo").isNotEmpty());
        }

        @Test
        @DisplayName("responde 404 si el negocio está inactivo")
        void negocioInactivo() throws Exception {
            long id = idCafeLaCeiba();
            jdbc.update("update negocio set activo = false where id = ?", id);

            mockMvc.perform(get("/api/negocios/{id}/menu", id))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.codigo").value("RECURSO_NO_ENCONTRADO"));
        }

        @Test
        @DisplayName("responde 400 si el id no es un número")
        void idNoNumerico() throws Exception {
            mockMvc.perform(get("/api/negocios/abc/menu"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.codigo").value("VALIDACION"))
                    .andExpect(jsonPath("$.errores[0].campo").value("id"))
                    .andExpect(jsonPath("$.errores[0].mensaje").value("Debe ser un número entero."));
        }

        @Test
        @DisplayName("responde 400 si el id no es positivo")
        void idNoPositivo() throws Exception {
            mockMvc.perform(get("/api/negocios/0/menu"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.codigo").value("VALIDACION"))
                    .andExpect(jsonPath("$.errores[0].campo").value("id"))
                    .andExpect(jsonPath("$.errores[0].mensaje").value("Debe ser un número mayor que cero."));
        }
    }
}
