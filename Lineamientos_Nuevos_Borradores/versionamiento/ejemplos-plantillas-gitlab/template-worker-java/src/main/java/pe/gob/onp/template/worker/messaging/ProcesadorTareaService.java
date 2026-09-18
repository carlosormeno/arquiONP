package pe.gob.onp.template.worker.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pe.gob.onp.template.worker.messaging.exception.TareaNoProcesableException;

/**
 * Stub de la lógica de negocio invocada por el consumidor Kafka. Cada equipo que adopte esta
 * plantilla reemplaza este cuerpo por el caso de uso real — el propósito aquí es solo mostrar
 * dónde encaja (equivalente a un puerto de entrada del dominio, ver
 * {@code onp-afiliacion-messaging} en {@code template-backend-java-modular} para el patrón
 * completo con Arquitectura Hexagonal si el sistema crece a Monolito Modular).
 */
@Component
public class ProcesadorTareaService {

    private static final Logger LOG = LoggerFactory.getLogger(ProcesadorTareaService.class);

    /**
     * Ejecuta la acción solicitada sobre el recurso indicado.
     *
     * @throws TareaNoProcesableException si el recurso no existe o la acción no aplica a su
     *      estado actual — error no recuperable, no reintentable (ver
     *      {@link pe.gob.onp.template.worker.config.KafkaConsumerErrorConfig}).
     */
    public void procesar(String recursoId, String accion) {
        if (recursoId == null || recursoId.isBlank()) {
            throw new TareaNoProcesableException("recursoId vacio: no se puede procesar la tarea");
        }
        // Reemplazar por la invocación real al caso de uso de negocio.
        LOG.info("Procesando tarea accion={} recurso.id={}", accion, recursoId);
    }
}
