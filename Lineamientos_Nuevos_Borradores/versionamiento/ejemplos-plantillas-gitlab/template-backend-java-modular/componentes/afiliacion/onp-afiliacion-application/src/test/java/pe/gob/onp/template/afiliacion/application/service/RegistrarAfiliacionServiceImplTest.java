package pe.gob.onp.template.afiliacion.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.gob.onp.common.domain.model.Dni;
import pe.gob.onp.template.afiliacion.domain.exception.AfiliadoYaRegistradoException;
import pe.gob.onp.template.afiliacion.domain.model.Afiliado;
import pe.gob.onp.template.afiliacion.domain.port.out.AfiliadoRepository;

/**
 * Unitaria con Mockito sobre el puerto de salida — sin contexto Spring
 * (LIN-TEST-001 §4.2, capa {@code application}).
 */
@ExtendWith(MockitoExtension.class)
class RegistrarAfiliacionServiceImplTest {

    @Mock
    private AfiliadoRepository afiliadoRepository;

    @Test
    void registrarConDniNuevoGuardaYRetornaAfiliado() {
        Dni dni = new Dni("12345678");
        when(afiliadoRepository.buscarPorDni(dni)).thenReturn(Optional.empty());
        when(afiliadoRepository.guardar(any(Afiliado.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegistrarAfiliacionServiceImpl service = new RegistrarAfiliacionServiceImpl(afiliadoRepository);
        Afiliado resultado = service.registrar(dni, "Juan Perez");

        assertThat(resultado.getDni()).isEqualTo(dni);
        verify(afiliadoRepository).guardar(any(Afiliado.class));
    }

    @Test
    void registrarConDniYaExistenteLanzaExcepcion() {
        Dni dni = new Dni("12345678");
        when(afiliadoRepository.buscarPorDni(dni))
                .thenReturn(Optional.of(Afiliado.registrar(dni, "Juan Perez")));

        RegistrarAfiliacionServiceImpl service = new RegistrarAfiliacionServiceImpl(afiliadoRepository);

        assertThatThrownBy(() -> service.registrar(dni, "Juan Perez"))
                .isInstanceOf(AfiliadoYaRegistradoException.class);
    }
}
