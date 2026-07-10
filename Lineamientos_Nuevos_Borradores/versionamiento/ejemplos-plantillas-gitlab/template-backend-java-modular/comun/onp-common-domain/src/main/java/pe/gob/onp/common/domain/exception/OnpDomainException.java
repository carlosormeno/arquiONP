package pe.gob.onp.common.domain.exception;

/**
 * Raíz de la jerarquía de excepciones de dominio institucional.
 * Toda excepción de negocio de cualquier componente debe extender de esta clase
 * (directa o indirectamente) para que el GlobalExceptionHandler del módulo -boot
 * pueda mapearla de forma uniforme al contrato de error de LIN-API-REST-001.
 */
public class OnpDomainException extends RuntimeException {

    public OnpDomainException(String message) {
        super(message);
    }

    public OnpDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
