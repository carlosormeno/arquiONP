package pe.gob.onp.template.defuncion.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import org.junit.jupiter.api.Test;

/** Unitaria pura de dominio — JUnit 5 sin Spring, sin Mockito de infra (LIN-DEV-JAVA-001 §14.1). */
class ConsultaDefuncionTest {

    @Test
    void registrarCreaConsultaConFechaActualYEstadoDado() {
        Dni dni = new Dni("12345678");

        ConsultaDefuncion consulta = ConsultaDefuncion.registrar(dni, EstadoDefuncion.VIVO);

        assertThat(consulta.getDni()).isEqualTo(dni);
        assertThat(consulta.getEstado()).isEqualTo(EstadoDefuncion.VIVO);
        assertThat(consulta.getId()).isNotNull();
        assertThat(consulta.getFechaConsulta()).isNotNull();
    }

    @Test
    void esVigenteDentroDeLasVeinticuatroHoras() {
        Instant fechaConsulta = Instant.now();
        ConsultaDefuncion consulta = ConsultaDefuncion.reconstruir(
                ConsultaDefuncionId.nuevo(), new Dni("12345678"), EstadoDefuncion.VIVO, fechaConsulta);

        boolean vigente = consulta.esVigente(fechaConsulta.plusSeconds(3600));

        assertThat(vigente).isTrue();
    }

    @Test
    void noEsVigenteDespuesDeVeinticuatroHoras() {
        Instant fechaConsulta = Instant.now();
        ConsultaDefuncion consulta = ConsultaDefuncion.reconstruir(
                ConsultaDefuncionId.nuevo(), new Dni("12345678"), EstadoDefuncion.VIVO, fechaConsulta);

        boolean vigente = consulta.esVigente(fechaConsulta.plusSeconds(25 * 3600));

        assertThat(vigente).isFalse();
    }

    @Test
    void dniInvalidoLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> new Dni("123"));
    }
}
