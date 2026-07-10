package pe.gob.onp.common.domain.exception;

/**
 * Excepción universal para "recurso de dominio no encontrado" (HTTP 404).
 * Cada componente de negocio puede lanzarla directamente o extenderla con
 * variantes específicas (ej. AfiliadoNoEncontradoException) según necesite.
 */
public class RecursoNoEncontradoException extends OnpDomainException {

    public RecursoNoEncontradoException(String message) {
        super(message);
    }
}
