package pe.gob.onp.template.worker.batch;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Unitaria pura — sin Spring, sin mocks (LIN-TEST-001 §4.6, fila "Table Module"): la clase bajo
 * prueba no depende de infraestructura, así que la prueba tampoco.
 */
class RegistroPendienteTableModuleTest {

    private final RegistroPendienteTableModule tableModule = new RegistroPendienteTableModule();

    @Test
    void expiraRegistrosPendientesConMasDeTreintaDiasDeAntiguedad() {
        Instant ahora = Instant.parse("2026-09-18T00:00:00Z");
        RegistroPendienteRow antiguo = new RegistroPendienteRow(
                1L, RegistroPendienteRow.ESTADO_PENDIENTE, ahora.minus(31, ChronoUnit.DAYS));
        RegistroPendienteRow reciente = new RegistroPendienteRow(
                2L, RegistroPendienteRow.ESTADO_PENDIENTE, ahora.minus(5, ChronoUnit.DAYS));

        List<RegistroPendienteRow> resultado =
                tableModule.aplicarReglaExpiracion(List.of(antiguo, reciente), ahora);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).id()).isEqualTo(1L);
        assertThat(resultado.get(0).estado()).isEqualTo(RegistroPendienteRow.ESTADO_EXPIRADO);
    }

    @Test
    void noReescribeRegistrosQueNoCambianDeEstado() {
        Instant ahora = Instant.parse("2026-09-18T00:00:00Z");
        RegistroPendienteRow yaExpirado = new RegistroPendienteRow(
                3L, RegistroPendienteRow.ESTADO_EXPIRADO, ahora.minus(90, ChronoUnit.DAYS));
        RegistroPendienteRow reciente = new RegistroPendienteRow(
                4L, RegistroPendienteRow.ESTADO_PENDIENTE, ahora.minus(1, ChronoUnit.DAYS));

        List<RegistroPendienteRow> resultado =
                tableModule.aplicarReglaExpiracion(List.of(yaExpirado, reciente), ahora);

        assertThat(resultado).isEmpty();
    }

    @Test
    void listaVaciaNoProduceCambios() {
        List<RegistroPendienteRow> resultado =
                tableModule.aplicarReglaExpiracion(List.of(), Instant.now());

        assertThat(resultado).isEmpty();
    }
}
