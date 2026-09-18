package pe.gob.onp.template.worker.messaging;

import java.time.Instant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import pe.gob.onp.template.worker.messaging.dedup.EventoProcesadoEntity;
import pe.gob.onp.template.worker.messaging.dedup.EventoProcesadoJpaRepository;
import pe.gob.onp.template.worker.messaging.dto.TareaSolicitadaEvent;

/**
 * EJEMPLO DE WORKER (consumidor de cola, proceso asíncrono continuo) — LIN-K8S-001 §4.1.
 * Manifiesto K8s correspondiente: {@code k8s/base/deployment.yaml}. Solo se activa con el perfil
 * Spring {@code worker} (ver {@link pe.gob.onp.template.worker.TemplateWorkerApplication}):
 * los pods que corren con el perfil {@code job} no crean este bean ni abren un listener
 * container, así que no intentan conectarse a Kafka.
 *
 * <p>Ack manual obligatorio (LIN-BUS-001 §8.1, §8.3): el commit automático confirmaría el offset
 * antes de que termine el procesamiento — si el proceso falla a mitad de camino, el mensaje se
 * perdería sin haberse aplicado.
 */
@Component
@Profile("worker")
public class TareaSolicitadaConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(TareaSolicitadaConsumer.class);

    private final ProcesadorTareaService procesadorTareaService;
    private final EventoProcesadoJpaRepository eventoProcesadoRepository;

    public TareaSolicitadaConsumer(
            ProcesadorTareaService procesadorTareaService,
            EventoProcesadoJpaRepository eventoProcesadoRepository) {
        this.procesadorTareaService = procesadorTareaService;
        this.eventoProcesadoRepository = eventoProcesadoRepository;
    }

    // Reemplazar "sistema.dominio.tarea-solicitada" por el tópico real del sistema.
    @KafkaListener(
            topics = "sistema.dominio.tarea-solicitada",
            groupId = "${spring.application.name}-grp")
    public void handle(ConsumerRecord<String, TareaSolicitadaEvent> record, Acknowledgment ack) {
        TareaSolicitadaEvent event = record.value();
        // Correlación en logs JSON (ver logback-spring.xml, MDC key "kafka.event.id") — no
        // captura excepciones, solo limpia el MDC; la propagación de errores hacia
        // DefaultErrorHandler no cambia (ver comentario más abajo).
        MDC.put("kafka.event.id", event.id());
        try {
            // Idempotencia por id del evento (LIN-BUS-001 §8.4) — no depende de que el caso de
            // uso sea naturalmente idempotente.
            if (eventoProcesadoRepository.existsById(event.id())) {
                LOG.info("Evento ya procesado, se ignora");
                ack.acknowledge();
                return;
            }

            procesadorTareaService.procesar(event.data().recursoId(), event.data().accion());
            eventoProcesadoRepository.save(new EventoProcesadoEntity(event.id(), Instant.now()));

            ack.acknowledge(); // commit SOLO si todo lo anterior salió bien
            // Si procesadorTareaService.procesar(...) lanza una excepción, esta línea nunca se
            // alcanza: no se hace ack y la excepción sube al DefaultErrorHandler (ver
            // KafkaConsumerErrorConfig), que decide reintentar o enviar a DLQ. No hay catch aquí
            // a propósito.
        } finally {
            MDC.remove("kafka.event.id");
        }
    }
}
