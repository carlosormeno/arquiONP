package pe.gob.onp.template.afiliacion.messaging.config;

import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;
import pe.gob.onp.template.afiliacion.domain.exception.AfiliadoNoEncontradoException;

/**
 * Reintentos + Dead Letter Queue (LIN-BUS-001 §8.5-8.6). El ejemplo de la norma usa
 * {@code ExponentialBackOffWithMaxRetries}, una clase que ya no existe en la versión
 * vigente de Spring Framework (6.x) — {@code setMaxAttempts(int)} se fusionó
 * directamente en {@code ExponentialBackOff}. {@code DefaultErrorHandler} gestiona
 * el {@code acknowledge} automáticamente tras agotar reintentos y delega en el
 * {@code recoverer}; el listener nunca necesita capturar la excepción para eso.
 */
@Configuration
public class KafkaConsumerErrorConfig {

    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<String, Object> kafkaTemplate) {
        // Publica en {tópico-original}.dlq — Kafka elige la partición (-1)
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, ex) -> new TopicPartition(record.topic() + ".dlq", -1));

        // 3 intentos, espera inicial 1s, multiplicador 2 (1s → 2s → 4s)
        ExponentialBackOff backOff = new ExponentialBackOff();
        backOff.setInitialInterval(1_000);
        backOff.setMultiplier(2.0);
        backOff.setMaxAttempts(3);

        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backOff);

        // No recuperable: el afiliadoId del evento no existe — reintentar no lo arregla.
        // Va directo a DLQ sin gastar los 3 intentos.
        handler.addNotRetryableExceptions(AfiliadoNoEncontradoException.class);

        return handler;
    }
}
