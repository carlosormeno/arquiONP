package pe.gob.onp.template.defuncion.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.onp.template.defuncion.application.usecase.ConsultarEstadoDefuncionServiceImpl;
import pe.gob.onp.template.defuncion.domain.port.in.ConsultarEstadoDefuncionUseCase;
import pe.gob.onp.template.defuncion.domain.port.out.ConsultaDefuncionRepository;
import pe.gob.onp.template.defuncion.domain.port.out.RegistroCivilDefuncionClient;

/**
 * Único punto donde el caso de uso POJO de este microservicio entra al contenedor de Spring
 * (LIN-DEV-JAVA-001 §14.1, "Regla de pureza hexagonal y desacoplamiento transaccional"). El bean
 * se expone con el TIPO DEL PUERTO, nunca con el tipo concreto — así, {@code infrastructure.web}
 * solo puede ver el contrato, nunca la implementación. La anotación {@code @Transactional} se
 * aplica aquí, sobre la definición del método {@code @Bean}, exactamente como exige la norma —
 * nunca dentro de {@code application.usecase}.
 */
@Configuration
public class UseCaseConfig {

    @Bean
    @Transactional
    public ConsultarEstadoDefuncionUseCase consultarEstadoDefuncionUseCase(
            ConsultaDefuncionRepository consultaDefuncionRepository,
            RegistroCivilDefuncionClient registroCivilDefuncionClient) {
        return new ConsultarEstadoDefuncionServiceImpl(consultaDefuncionRepository, registroCivilDefuncionClient);
    }
}
