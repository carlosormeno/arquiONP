package pe.gob.onp.template.defuncion.domain.port.out;

import pe.gob.onp.template.defuncion.domain.model.Dni;
import pe.gob.onp.template.defuncion.domain.model.EstadoDefuncion;

/**
 * Puerto de salida hacia el sistema externo de Registro Civil (RENIEC). El dominio define QUÉ
 * necesita — "necesito saber el estado vital de una persona por DNI" — sin saber que la
 * implementación real viaja por HTTP, está protegida por un Circuit Breaker y un Bulkhead
 * (LIN-DIS-001 §6.2/§6.3, obligatorios en Microservicio, `DIS-R-009`), o que puede lanzar
 * {@link pe.gob.onp.template.defuncion.domain.exception.ServicioRegistroCivilNoDisponibleException}
 * si el circuito está abierto. Esta interfaz —igual que {@link ConsultaDefuncionRepository}— no
 * tiene ningún import de Spring, Resilience4j ni de cliente HTTP: eso vive exclusivamente en el
 * Adapter ({@code infrastructure.client.RegistroCivilHttpAdapter}).
 */
public interface RegistroCivilDefuncionClient {

    EstadoDefuncion consultar(Dni dni);
}
