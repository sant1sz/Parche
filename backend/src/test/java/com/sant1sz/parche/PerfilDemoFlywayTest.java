package com.sant1sz.parche;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.exception.FlywayValidateException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Una base que ya cargó los datos demo debe poder arrancar después sin el perfil demo.
 * Usa la misma configuración de Flyway que la aplicación (application.properties).
 */
@DisplayName("Flyway con y sin el perfil demo sobre la misma base")
class PerfilDemoFlywayTest {

    private static final String MIGRACIONES = "classpath:db/migration";
    private static final String DEMO = "classpath:db/demo";

    private static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18-alpine");

    @BeforeAll
    static void iniciar() {
        postgres.start();
    }

    @AfterAll
    static void detener() {
        postgres.stop();
    }

    @Test
    @DisplayName("primero con demo y luego sin demo: valida y no aplica nada nuevo")
    void arrancaSinDemoDespuesDeDemo() throws IOException {
        String patrones = propiedadDeLaAplicacion("spring.flyway.ignore-migration-patterns");

        // Primer arranque, con el perfil demo.
        flyway(patrones, MIGRACIONES, DEMO).migrate();

        // Sin la propiedad, Flyway se niega a arrancar: la migración repetible no existe localmente.
        assertThatThrownBy(() -> flyway("*:future", MIGRACIONES).validate())
                .isInstanceOf(FlywayValidateException.class)
                .hasMessageContaining("demo cafe la ceiba");

        // Segundo arranque, sin el perfil demo y con la configuración real: arranca bien.
        Flyway sinDemo = flyway(patrones, MIGRACIONES);
        sinDemo.validate();
        assertThat(sinDemo.migrate().migrationsExecuted).isZero();
    }

    private static Flyway flyway(String patronesIgnorados, String... ubicaciones) {
        return Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .locations(ubicaciones)
                .ignoreMigrationPatterns(patronesIgnorados.split(","))
                .load();
    }

    private static String propiedadDeLaAplicacion(String clave) throws IOException {
        Properties propiedades = new Properties();
        try (InputStream entrada = PerfilDemoFlywayTest.class.getResourceAsStream("/application.properties")) {
            propiedades.load(entrada);
        }
        String valor = propiedades.getProperty(clave);
        assertThat(valor).as("%s en application.properties", clave).isNotBlank();
        return valor;
    }
}
