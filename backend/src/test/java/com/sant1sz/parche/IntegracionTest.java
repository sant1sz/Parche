package com.sant1sz.parche;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base de las pruebas de integración: aplicación completa contra un PostgreSQL real
 * (Testcontainers), con las migraciones de Flyway y los datos demo de Café La Ceiba.
 * Cada prueba corre en una transacción que se revierte al final, así que los cambios
 * que haga sobre los datos no afectan a las demás.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("demo")
@Import(TestcontainersConfiguration.class)
@Transactional
public abstract class IntegracionTest {

    protected static final String SLUG_DEMO = "cafe-la-ceiba";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected JdbcTemplate jdbc;

    protected long idCafeLaCeiba() {
        return jdbc.queryForObject("select id from negocio where slug = ?", Long.class, SLUG_DEMO);
    }
}
