package com.sant1sz.parche;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Verifica el resultado de las migraciones de Flyway y de los datos demo. */
@DisplayName("Migraciones y datos demo")
class MigracionesTest extends IntegracionTest {

    @Test
    @DisplayName("Café La Ceiba tiene 12 mesas, 5 categorías y 30 productos, y está marcado como demo")
    void datosDemoCompletos() {
        long id = idCafeLaCeiba();
        assertThat(jdbc.queryForObject("select es_demo from negocio where id = ?", Boolean.class, id)).isTrue();
        assertThat(contar("mesa", id)).isEqualTo(12);
        assertThat(contar("categoria", id)).isEqualTo(5);
        assertThat(contar("producto", id)).isEqualTo(30);
    }

    @Test
    @DisplayName("los datos demo se cargan como migración repetible, sin número de versión")
    void demoEsRepetible() {
        assertThat(jdbc.queryForList(
                "select version from flyway_schema_history where script = 'R__demo_cafe_la_ceiba.sql'",
                String.class))
                .hasSize(1)
                .containsOnlyNulls();
    }

    @Test
    @DisplayName("los precios se guardan como BIGINT y son positivos")
    void preciosEnteros() {
        assertThat(jdbc.queryForObject(
                "select data_type from information_schema.columns where table_name = 'producto' and column_name = 'precio'",
                String.class)).isEqualTo("bigint");
        assertThat(jdbc.queryForObject("select min(precio) from producto", Long.class)).isPositive();
    }

    private int contar(String tabla, long idNegocio) {
        return jdbc.queryForObject(
                "select count(*) from " + tabla + " where id_negocio = ?", Integer.class, idNegocio);
    }
}
