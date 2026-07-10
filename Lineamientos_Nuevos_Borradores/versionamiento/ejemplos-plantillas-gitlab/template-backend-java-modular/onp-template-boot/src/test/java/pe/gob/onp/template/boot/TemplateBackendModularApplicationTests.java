package pe.gob.onp.template.boot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.OracleContainer;

/**
 * Prueba de integración full (LIN-TEST-001 §4.3, componente Hexagonal/Microservicio):
 * levanta todo el contexto de Spring ensamblado (todos los -api/-infrastructure de
 * los componentes activos) contra un Oracle real por contenedor.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class TemplateBackendModularApplicationTests {

    @Container
    static final OracleContainer ORACLE = new OracleContainer("gvenzl/oracle-xe:21-slim-faststart");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", ORACLE::getJdbcUrl);
        registry.add("spring.datasource.username", ORACLE::getUsername);
        registry.add("spring.datasource.password", ORACLE::getPassword);
    }

    @Test
    void contextLoads() {
        // Si el contexto no ensambla correctamente los 5 módulos (común + 4 de afiliacion),
        // esta prueba falla al arrancar — es la única red de seguridad end-to-end del ensamblaje.
    }
}
