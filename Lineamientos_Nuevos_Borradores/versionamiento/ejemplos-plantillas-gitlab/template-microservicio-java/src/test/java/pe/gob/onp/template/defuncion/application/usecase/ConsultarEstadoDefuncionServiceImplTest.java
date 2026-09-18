package pe.gob.onp.template.defuncion.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.gob.onp.template.defuncion.domain.model.ConsultaDefuncion;
import pe.gob.onp.template.defuncion.domain.model.ConsultaDefuncionId;
import pe.gob.onp.template.defuncion.domain.model.Dni;
import pe.gob.onp.template.defuncion.domain.model.EstadoDefuncion;
import pe.gob.onp.template.defuncion.domain.port.out.ConsultaDefuncionRepository;
import pe.gob.onp.template.defuncion.domain.port.out.RegistroCivilDefuncionClient;

/**
 * Prueba unitaria del caso de uso — POJO, sin Spring (LIN-DEV-JAVA-001 §14.1). Los puertos de
 * salida se mockean con Mockito; el caso de uso NO sabe que son mocks, exactamente como en
 * producción no sabría que son adapters JPA/HTTP reales.
 */
@ExtendWith(MockitoExtension.class)
class ConsultarEstadoDefuncionServiceImplTest {

    @Mock
    private ConsultaDefuncionRepository consultaDefuncionRepository;

    @Mock
    private RegistroCivilDefuncionClient registroCivilDefuncionClient;

    private ConsultarEstadoDefuncionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ConsultarEstadoDefuncionServiceImpl(consultaDefuncionRepository, registroCivilDefuncionClient);
    }

    @Test
    void sinConsultaPreviaConsultaAlRegistroCivilYPersisteElResultado() {
        Dni dni = new Dni("12345678");
        when(consultaDefuncionRepository.buscarUltimaPorDni(dni)).thenReturn(Optional.empty());
        when(registroCivilDefuncionClient.consultar(dni)).thenReturn(EstadoDefuncion.VIVO);
        when(consultaDefuncionRepository.guardar(any(ConsultaDefuncion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ConsultaDefuncion resultado = service.consultar(dni);

        assertThat(resultado.getEstado()).isEqualTo(EstadoDefuncion.VIVO);
        verify(registroCivilDefuncionClient, times(1)).consultar(dni);
        verify(consultaDefuncionRepository, times(1)).guardar(any(ConsultaDefuncion.class));
    }

    @Test
    void conConsultaPreviaVigenteReutilizaElResultadoSinConsultarDeNuevo() {
        Dni dni = new Dni("87654321");
        ConsultaDefuncion previa = ConsultaDefuncion.registrar(dni, EstadoDefuncion.VIVO);
        when(consultaDefuncionRepository.buscarUltimaPorDni(dni)).thenReturn(Optional.of(previa));

        ConsultaDefuncion resultado = service.consultar(dni);

        assertThat(resultado).isEqualTo(previa);
        verify(registroCivilDefuncionClient, never()).consultar(any(Dni.class));
        verify(consultaDefuncionRepository, never()).guardar(any(ConsultaDefuncion.class));
    }

    @Test
    void conConsultaPreviaVencidaVuelveAConsultar() {
        Dni dni = new Dni("11223344");
        ConsultaDefuncion vencida = ConsultaDefuncion.reconstruir(
                ConsultaDefuncionId.nuevo(), dni, EstadoDefuncion.VIVO, Instant.now().minusSeconds(25 * 3600));
        when(consultaDefuncionRepository.buscarUltimaPorDni(dni)).thenReturn(Optional.of(vencida));
        when(registroCivilDefuncionClient.consultar(dni)).thenReturn(EstadoDefuncion.FALLECIDO);
        when(consultaDefuncionRepository.guardar(any(ConsultaDefuncion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ConsultaDefuncion resultado = service.consultar(dni);

        assertThat(resultado.getEstado()).isEqualTo(EstadoDefuncion.FALLECIDO);
        verify(registroCivilDefuncionClient, times(1)).consultar(dni);
    }
}
