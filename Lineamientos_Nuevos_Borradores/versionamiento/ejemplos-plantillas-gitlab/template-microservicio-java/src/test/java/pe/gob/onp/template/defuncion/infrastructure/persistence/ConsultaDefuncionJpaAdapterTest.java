package pe.gob.onp.template.defuncion.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import pe.gob.onp.template.defuncion.domain.model.ConsultaDefuncion;
import pe.gob.onp.template.defuncion.domain.model.Dni;
import pe.gob.onp.template.defuncion.domain.model.EstadoDefuncion;

/**
 * Prueba unitaria del adapter de persistencia — a diferencia de
 * {@code AfiliadoJpaAdapterTest} en `template-backend-java-modular` (que usa
 * {@code @DataJpaTest} + Testcontainers Oracle real), aquí se mockea
 * {@link ConsultaDefuncionJpaRepository} directamente. Decisión documentada en el README de
 * esta plantilla ("Qué NO incluye este scaffold"): igual que ya resolvió `template-worker-java`
 * en esta misma sesión, Testcontainers exige Docker con acceso de red para descargar la imagen
 * `gvenzl/oracle-xe`, no disponible en el entorno de verificación offline de este scaffold. Un
 * equipo que adopte esta plantilla debe agregar su propia prueba de integración con
 * Testcontainers Oracle antes de pasar a QA (LIN-TEST-001).
 */
@ExtendWith(MockitoExtension.class)
class ConsultaDefuncionJpaAdapterTest {

    @Mock
    private ConsultaDefuncionJpaRepository jpaRepository;

    private final ConsultaDefuncionMapper mapper = new ConsultaDefuncionMapper();

    private ConsultaDefuncionJpaAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ConsultaDefuncionJpaAdapter(jpaRepository, mapper);
    }

    @Test
    void guardarTraduceDominioAEntidadYDevuelveElDominioReconstruido() {
        ConsultaDefuncion consulta = ConsultaDefuncion.registrar(new Dni("12345678"), EstadoDefuncion.VIVO);
        when(jpaRepository.save(any(ConsultaDefuncionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ConsultaDefuncion guardado = adapter.guardar(consulta);

        assertThat(guardado.getDni()).isEqualTo(consulta.getDni());
        assertThat(guardado.getEstado()).isEqualTo(EstadoDefuncion.VIVO);
    }

    @Test
    void buscarUltimaPorDniConRegistroDevuelvePresente() {
        Dni dni = new Dni("87654321");
        ConsultaDefuncionEntity entity =
                new ConsultaDefuncionEntity(UUID.randomUUID(), dni.valor(), "FALLECIDO", Instant.now());
        when(jpaRepository.findByDniOrderByFechaConsultaDesc(eq(dni.valor()), any(Pageable.class)))
                .thenReturn(List.of(entity));

        Optional<ConsultaDefuncion> encontrado = adapter.buscarUltimaPorDni(dni);

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getEstado()).isEqualTo(EstadoDefuncion.FALLECIDO);
    }

    @Test
    void buscarUltimaPorDniSinRegistroDevuelveVacio() {
        Dni dni = new Dni("11223344");
        when(jpaRepository.findByDniOrderByFechaConsultaDesc(eq(dni.valor()), any(Pageable.class)))
                .thenReturn(List.of());

        Optional<ConsultaDefuncion> encontrado = adapter.buscarUltimaPorDni(dni);

        assertThat(encontrado).isEmpty();
    }
}
