package pe.gob.onp.template.afiliacion.messaging;

import java.time.Instant;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import pe.gob.onp.template.afiliacion.domain.model.AfiliadoId;
import pe.gob.onp.template.afiliacion.domain.port.in.ActivarAfiliacionPorAporteUseCase;
import pe.gob.onp.template.afiliacion.messaging.dedup.EventoProcesadoEntity;
import pe.gob.onp.template.afiliacion.messaging.dedup.EventoProcesadoJpaRepository;
import pe.gob.onp.template.afiliacion.messaging.dto.AporteRegistradoEvent;

/**
 * Adapter de entrada (driving) — el equivalente Kafka de un {@code @RestController}.
 * Solo conoce el puerto de entrada del dominio, igual que
 * {@code AfiliacionController} en -api (LIN-DEV-JAVA-001 §14.1).
 *
 * <p>Ack manual obligatorio (LIN-BUS-001 §8.1, §8.3): el commit automático
 * confirmaría el offset antes de que termine el procesamiento — si el proceso
 * falla a mitad de camino, el mensaje se perdería sin haberse aplicado.
 */
@Component
public class AporteRegistradoConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(AporteRegistradoConsumer.class);

    private final ActivarAfiliacionPorAporteUseCase activarAfiliacionUseCase;
    private final EventoProcesadoJpaRepository eventoProcesadoRepository;

    public AporteRegistradoConsumer(
            ActivarAfiliacionPorAporteUseCase activarAfiliacionUseCase,
            EventoProcesadoJpaRepository eventoProcesadoRepository) {
        this.activarAfiliacionUseCase = activarAfiliacionUseCase;
        this.eventoProcesadoRepository = eventoProcesadoRepository;
    }

    @KafkaListener(
            topics = "aportes.registro.aporte-registrado",
            groupId = "${spring.application.name}-grp")
    public void handle(ConsumerRecord<String, AporteRegistradoEvent> record, Acknowledgment ack) {
        AporteRegistradoEvent event = record.value();

        // Idempotencia por id del evento (LIN-BUS-001 §8.4) — no depende de que el
        // caso de uso sea naturalmente idempotente; funciona igual para cualquier evento futuro.
        if (eventoProcesadoRepository.existsById(event.id())) {
            LOG.info("Evento {} ya procesado, se ignora", event.id());
            ack.acknowledge();
            return;
        }

        AfiliadoId afiliadoId = new AfiliadoId(UUID.fromString(event.data().afiliadoId()));
        activarAfiliacionUseCase.activarPorAporte(afiliadoId);
        eventoProcesadoRepository.save(new EventoProcesadoEntity(event.id(), Instant.now()));

        ack.acknowledge(); // commit SOLO si todo lo anterior salió bien
        // Si activarAfiliacionUseCase lanza una excepción, esta línea nunca se alcanza:
        // no se hace ack y la excepción sube al DefaultErrorHandler (ver KafkaConsumerErrorConfig),
        // que decide reintentar o enviar a DLQ. No hay try/catch aquí a propósito.
    }
}
