package pe.gob.onp.template.afiliacion.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.onp.template.afiliacion.application.service.ActivarAfiliacionPorAporteServiceImpl;
import pe.gob.onp.template.afiliacion.application.service.RegistrarAfiliacionServiceImpl;
import pe.gob.onp.template.afiliacion.domain.port.in.ActivarAfiliacionPorAporteUseCase;
import pe.gob.onp.template.afiliacion.domain.port.in.RegistrarAfiliacionUseCase;
import pe.gob.onp.template.afiliacion.domain.port.out.AfiliadoRepository;

/**
 * Único punto donde los casos de uso POJO del componente entran al contenedor
 * de Spring (LIN-DEV-JAVA-001 §14.1). Cada bean se expone con el TIPO DEL
 * PUERTO, nunca con el tipo concreto — así, tanto -api (HTTP) como -messaging
 * (Kafka) de este módulo, o -application de otro módulo (regla de gobernanza
 * #2), solo pueden ver el contrato, nunca la implementación.
 */
@Configuration
public class AfiliacionUseCaseConfig {

    @Bean
    @Transactional
    public RegistrarAfiliacionUseCase registrarAfiliacionUseCase(AfiliadoRepository afiliadoRepository) {
        return new RegistrarAfiliacionServiceImpl(afiliadoRepository);
    }

    @Bean
    @Transactional
    public ActivarAfiliacionPorAporteUseCase activarAfiliacionPorAporteUseCase(AfiliadoRepository afiliadoRepository) {
        return new ActivarAfiliacionPorAporteServiceImpl(afiliadoRepository);
    }
}
