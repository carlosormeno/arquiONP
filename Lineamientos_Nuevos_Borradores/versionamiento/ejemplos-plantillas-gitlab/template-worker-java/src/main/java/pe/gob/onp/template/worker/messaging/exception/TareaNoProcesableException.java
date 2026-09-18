package pe.gob.onp.template.worker.messaging.exception;

/**
 * Error de negocio no recuperable: el recurso referenciado por el evento no existe, o la acción
 * solicitada no aplica a su estado actual. Reintentar el mensaje no cambia el resultado, así que
 * {@link pe.gob.onp.template.worker.config.KafkaConsumerErrorConfig} lo excluye de los 3
 * reintentos y lo envía directo a la Dead Letter Queue (LIN-BUS-001 §8.6).
 */
public class TareaNoProcesableException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public TareaNoProcesableException(String mensaje) {
        super(mensaje);
    }
}
