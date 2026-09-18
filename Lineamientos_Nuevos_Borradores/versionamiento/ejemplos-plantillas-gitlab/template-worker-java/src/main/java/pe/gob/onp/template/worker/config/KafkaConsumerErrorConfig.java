package pe.gob.onp.template.worker.config;

import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;
import pe.gob.onp.template.worker.messaging.exception.TareaNoProcesableException;

/**
 * Reintentos + Dead Letter Queue (LIN-BUS-001 §8.5-8.6), mismo patrón que
 * {@code onp-afiliacion-messaging} en {@code template-backend-java-modular} pero con el ejemplo
 * de negocio de este scaffold. {@code DefaultErrorHandler} gestiona el {@code acknowledge}
 * automáticamente tras agotar reintentos y delega en el {@code recoverer}; el listener nunca
 * necesita capturar la excepción para eso (ver
 * {@link pe.gob.onp.template.worker.messaging.TareaSolicitadaConsumer}).
 *
 * <p>Este bean se registra siempre, sin {@code @Profile}, aunque solo lo use el perfil
 * {@code worker}: la creación de {@link DeadLetterPublishingRecoverer} y del
 * {@link KafkaTemplate} que envuelve es perezosa (no abre conexión al broker en el arranque),
 * así que no tiene costo ni riesgo para los pods que corren con el perfil {@code job} sin
 * necesitar Kafka.
 */
@Configuration
public class KafkaConsumerErrorConfig {

    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<String, Object> kafkaTemplate) {
        // Publica en {tópico-original}.dlq — Kafka elige la partición (-1)
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, ex) -> new TopicPartition(record.topic() + ".dlq", -1));

        // 3 intentos, espera inicial 1s, multiplicador 2 (1s -> 2s -> 4s)
        ExponentialBackOff backOff = new ExponentialBackOff();
        backOff.setInitialInterval(1_000);
        backOff.setMultiplier(2.0);
        backOff.setMaxAttempts(3);

        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backOff);

        // No recuperable: el recurso referenciado por el evento no existe o la acción no aplica
        // — reintentar no lo arregla. Va directo a DLQ sin gastar los 3 intentos.
        handler.addNotRetryableExceptions(TareaNoProcesableException.class);

        return handler;
    }
}
