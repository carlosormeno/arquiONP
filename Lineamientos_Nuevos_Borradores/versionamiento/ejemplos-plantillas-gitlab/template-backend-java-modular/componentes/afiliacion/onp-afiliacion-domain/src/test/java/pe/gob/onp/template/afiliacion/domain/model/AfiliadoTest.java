package pe.gob.onp.template.afiliacion.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import pe.gob.onp.common.domain.model.Dni;

/**
 * Prueba unitaria pura — sin Spring, sin Mockito de infraestructura (LIN-DIS-001 §2.2,
 * LIN-TEST-001 §4.3). Verifica invariantes del Agregado Raíz y sus transiciones de estado.
 */
class AfiliadoTest {

    @Test
    void registrarCreaAfiliadoEnEstadoRegistrado() {
        Afiliado afiliado = Afiliado.registrar(new Dni("12345678"), "Juan Perez");

        assertThat(afiliado.getEstado()).isEqualTo(EstadoAfiliado.REGISTRADO);
        assertThat(afiliado.getDni().valor()).isEqualTo("12345678");
    }

    @Test
    void activarDesdeRegistradoTransicionaAActivo() {
        Afiliado afiliado = Afiliado.registrar(new Dni("12345678"), "Juan Perez");

        afiliado.activar();

        assertThat(afiliado.getEstado()).isEqualTo(EstadoAfiliado.ACTIVO);
    }

    @Test
    void suspenderDesdeRegistradoLanzaExcepcion() {
        Afiliado afiliado = Afiliado.registrar(new Dni("12345678"), "Juan Perez");

        assertThatThrownBy(afiliado::suspender)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("suspender");
    }

    @Test
    void registrarConNombreVacioLanzaExcepcion() {
        assertThatThrownBy(() -> Afiliado.registrar(new Dni("12345678"), " "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
