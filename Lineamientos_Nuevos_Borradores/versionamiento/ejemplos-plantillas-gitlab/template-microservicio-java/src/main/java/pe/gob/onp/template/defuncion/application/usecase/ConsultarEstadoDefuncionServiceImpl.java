package pe.gob.onp.template.defuncion.application.usecase;

import java.time.Instant;
import java.util.Optional;
import pe.gob.onp.template.defuncion.domain.model.ConsultaDefuncion;
import pe.gob.onp.template.defuncion.domain.model.Dni;
import pe.gob.onp.template.defuncion.domain.model.EstadoDefuncion;
import pe.gob.onp.template.defuncion.domain.port.in.ConsultarEstadoDefuncionUseCase;
import pe.gob.onp.template.defuncion.domain.port.out.ConsultaDefuncionRepository;
import pe.gob.onp.template.defuncion.domain.port.out.RegistroCivilDefuncionClient;

/**
 * Implementación pura del caso de uso — POJO sin {@code @Service}, sin {@code @Transactional}
 * ni ningún estereotipo de Spring (LIN-DEV-JAVA-001 §14.1, "Regla de pureza hexagonal"). Recibe
 * sus puertos de salida por inyección de constructor. El cableado hacia el contenedor de Spring
 * (incluyendo la demarcación transaccional) ocurre exclusivamente en la capa
 * {@code infrastructure.config}, en la clase {@code @Configuration} que declara este servicio
 * como {@code @Bean}.
 *
 * <p>Regla de negocio: reutiliza la última consulta si sigue vigente (24 horas,
 * {@link ConsultaDefuncion#esVigente(Instant)}) en vez de golpear siempre al Registro Civil —
 * reduce la presión sobre un punto de salida que, por ser Microservicio, ya paga el costo de un
 * Circuit Breaker + Bulkhead obligatorios (LIN-DIS-001 §6.2/§6.3).</p>
 */
public class ConsultarEstadoDefuncionServiceImpl implements ConsultarEstadoDefuncionUseCase {

    private final ConsultaDefuncionRepository consultaDefuncionRepository;
    private final RegistroCivilDefuncionClient registroCivilDefuncionClient;

    public ConsultarEstadoDefuncionServiceImpl(
            ConsultaDefuncionRepository consultaDefuncionRepository,
            RegistroCivilDefuncionClient registroCivilDefuncionClient) {
        this.consultaDefuncionRepository = consultaDefuncionRepository;
        this.registroCivilDefuncionClient = registroCivilDefuncionClient;
    }

    @Override
    public ConsultaDefuncion consultar(Dni dni) {
        Optional<ConsultaDefuncion> ultima = consultaDefuncionRepository.buscarUltimaPorDni(dni);
        if (ultima.isPresent() && ultima.get().esVigente(Instant.now())) {
            return ultima.get();
        }

        EstadoDefuncion estado = registroCivilDefuncionClient.consultar(dni);
        ConsultaDefuncion consulta = ConsultaDefuncion.registrar(dni, estado);
        return consultaDefuncionRepository.guardar(consulta);
    }
}
