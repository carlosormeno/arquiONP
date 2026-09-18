package pe.gob.onp.template.defuncion.domain.exception;

/**
 * Raíz de la jerarquía de excepciones de dominio de este microservicio. Toda excepción de
 * negocio debe extender de esta clase (directa o indirectamente) para que
 * {@code infrastructure.web.GlobalExceptionHandler} pueda mapearla de forma uniforme al
 * contrato de error de LIN-API-REST-001.
 *
 * <p>Al ser un microservicio independiente (no un componente de `template-backend-java-modular`),
 * esta jerarquía es propia — no extiende ninguna clase de un Shared Kernel externo.</p>
 */
public class ConsultaDefuncionDomainException extends RuntimeException {

    public ConsultaDefuncionDomainException(String message) {
        super(message);
    }

    public ConsultaDefuncionDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
