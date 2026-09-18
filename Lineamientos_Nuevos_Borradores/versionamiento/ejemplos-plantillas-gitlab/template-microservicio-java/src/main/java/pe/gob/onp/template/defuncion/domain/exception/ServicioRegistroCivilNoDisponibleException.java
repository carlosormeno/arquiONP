package pe.gob.onp.template.defuncion.domain.exception;

/**
 * Excepción de negocio lanzada cuando el punto de salida hacia el Registro Civil (RENIEC) no
 * puede responder — ya sea por fallo de red, timeout, o porque el Circuit Breaker está en
 * estado ABIERTO (LIN-DIS-001 §6.2, `DIS-R-009`). Es lanzada por el *fallback* del adapter de
 * infraestructura ({@code infrastructure.client.RegistroCivilHttpAdapter}) y nunca por el
 * dominio directamente — el dominio solo conoce el contrato del puerto de salida
 * ({@code domain.port.out.RegistroCivilDefuncionClient}), no la causa técnica del fallo.
 */
public class ServicioRegistroCivilNoDisponibleException extends ConsultaDefuncionDomainException {

    public ServicioRegistroCivilNoDisponibleException(String dni, Throwable cause) {
        super("El servicio de Registro Civil no está disponible para verificar el DNI: " + dni, cause);
    }
}
