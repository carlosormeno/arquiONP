package pe.gob.onp.template.afiliacion.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.OracleContainer;
import pe.gob.onp.common.domain.model.Dni;
import pe.gob.onp.template.afiliacion.domain.model.Afiliado;
import pe.gob.onp.template.afiliacion.domain.model.AfiliadoId;
import pe.gob.onp.template.afiliacion.domain.port.out.AfiliadoRepository;

/**
 * Integración con BD real (LIN-TEST-001 §11.1: {@code gvenzl/oracle-xe:21-slim-faststart}),
 * no con mocks — verifica el adaptador JPA completo, incluyendo el mapeo Entity/Dominio.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({AfiliadoMapper.class, AfiliadoJpaAdapter.class})
@Testcontainers
class AfiliadoJpaAdapterTest {

    @Container
    static final OracleContainer ORACLE = new OracleContainer("gvenzl/oracle-xe:21-slim-faststart");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", ORACLE::getJdbcUrl);
        registry.add("spring.datasource.username", ORACLE::getUsername);
        registry.add("spring.datasource.password", ORACLE::getPassword);
    }

    @Autowired
    private AfiliadoRepository afiliadoRepository;

    @Test
    void guardarYBuscarPorDniDevuelveElMismoAfiliado() {
        Dni dni = new Dni("12345678");
        Afiliado nuevo = Afiliado.registrar(dni, "Juan Perez");

        afiliadoRepository.guardar(nuevo);
        Optional<Afiliado> encontrado = afiliadoRepository.buscarPorDni(dni);

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getNombreCompleto()).isEqualTo("Juan Perez");
    }

    @Test
    void buscarPorDniSinRegistroDevuelveVacio() {
        Optional<Afiliado> encontrado = afiliadoRepository.buscarPorDni(new Dni("87654321"));

        assertThat(encontrado).isEmpty();
    }

    @Test
    void guardarYBuscarPorIdDevuelveElMismoAfiliado() {
        Afiliado nuevo = Afiliado.registrar(new Dni("11223344"), "Maria Lopez");

        Afiliado guardado = afiliadoRepository.guardar(nuevo);
        Optional<Afiliado> encontrado = afiliadoRepository.buscarPorId(guardado.getId());

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getDni().valor()).isEqualTo("11223344");
    }

    @Test
    void buscarPorIdSinRegistroDevuelveVacio() {
        Optional<Afiliado> encontrado = afiliadoRepository.buscarPorId(AfiliadoId.nuevo());

        assertThat(encontrado).isEmpty();
    }
}
