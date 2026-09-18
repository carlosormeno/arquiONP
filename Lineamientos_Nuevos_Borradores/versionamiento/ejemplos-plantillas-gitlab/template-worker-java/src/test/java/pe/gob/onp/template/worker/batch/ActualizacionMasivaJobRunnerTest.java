package pe.gob.onp.template.worker.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;

/**
 * Prueba {@code ActualizacionMasivaJobRunner#ejecutar(...)} directamente — nunca
 * {@code ActualizacionMasivaJobRunner#run(...)}, que termina la JVM con {@code System.exit} (ver
 * Javadoc de la clase bajo prueba). {@code JdbcTemplate} mockeado: no requiere Oracle real ni
 * Testcontainers para esta prueba unitaria.
 */
@ExtendWith(MockitoExtension.class)
class ActualizacionMasivaJobRunnerTest {

    private static final int TAMANIO_PAGINA = 500;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Test
    @SuppressWarnings("unchecked")
    void ejecutarDevuelveCeroYActualizaEnLoteCuandoHayRegistrosExpirados() {
        RegistroPendienteRow antiguo = new RegistroPendienteRow(
                1L, RegistroPendienteRow.ESTADO_PENDIENTE, Instant.now().minus(40, ChronoUnit.DAYS));
        stubLecturaPendientes().thenReturn(List.of(antiguo));

        ActualizacionMasivaJobRunner runner = new ActualizacionMasivaJobRunner(
                jdbcTemplate, new RegistroPendienteTableModule(), TAMANIO_PAGINA);

        int codigoSalida = runner.ejecutar(new DefaultApplicationArguments());

        assertThat(codigoSalida).isEqualTo(0);
        verify(jdbcTemplate).batchUpdate(
                anyString(), any(List.class), eq(1), any(ParameterizedPreparedStatementSetter.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void ejecutarNoEscribeNadaCuandoNoHayRegistrosExpirados() {
        RegistroPendienteRow reciente = new RegistroPendienteRow(
                2L, RegistroPendienteRow.ESTADO_PENDIENTE, Instant.now().minus(1, ChronoUnit.DAYS));
        stubLecturaPendientes().thenReturn(List.of(reciente));

        ActualizacionMasivaJobRunner runner = new ActualizacionMasivaJobRunner(
                jdbcTemplate, new RegistroPendienteTableModule(), TAMANIO_PAGINA);

        int codigoSalida = runner.ejecutar(new DefaultApplicationArguments());

        assertThat(codigoSalida).isEqualTo(0);
        verifyNuncaEscribio();
    }

    @Test
    @SuppressWarnings("unchecked")
    void ejecutarDevuelveUnoCuandoFallaLaLecturaYNoIntentaEscribir() {
        stubLecturaPendientes().thenThrow(new DataAccessResourceFailureException("BD no disponible"));

        ActualizacionMasivaJobRunner runner = new ActualizacionMasivaJobRunner(
                jdbcTemplate, new RegistroPendienteTableModule(), TAMANIO_PAGINA);

        int codigoSalida = runner.ejecutar(new DefaultApplicationArguments());

        assertThat(codigoSalida).isEqualTo(1);
        verifyNuncaEscribio();
    }

    @SuppressWarnings("unchecked")
    private OngoingStubbing<List<RegistroPendienteRow>> stubLecturaPendientes() {
        return when(jdbcTemplate.query(
                anyString(),
                any(RowMapper.class),
                eq(RegistroPendienteRow.ESTADO_PENDIENTE),
                eq(TAMANIO_PAGINA)));
    }

    @SuppressWarnings("unchecked")
    private void verifyNuncaEscribio() {
        verify(jdbcTemplate, never()).batchUpdate(
                anyString(), any(List.class), any(Integer.class), any(ParameterizedPreparedStatementSetter.class));
    }
}
