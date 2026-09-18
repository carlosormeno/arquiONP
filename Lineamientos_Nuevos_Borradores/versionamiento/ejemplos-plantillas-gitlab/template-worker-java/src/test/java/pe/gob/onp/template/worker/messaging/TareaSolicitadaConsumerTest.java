package pe.gob.onp.template.worker.messaging;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;
import pe.gob.onp.template.worker.messaging.dedup.EventoProcesadoJpaRepository;
import pe.gob.onp.template.worker.messaging.dto.TareaSolicitadaEvent;
import pe.gob.onp.template.worker.messaging.dto.TareaSolicitadaEvent.TareaSolicitadaData;
import pe.gob.onp.template.worker.messaging.exception.TareaNoProcesableException;

/**
 * Unitaria con Mockito — sin Kafka real, sin contexto Spring
 * (LIN-TEST-001 §4.5, fila "Handler / lógica de procesamiento del mensaje").
 * Verifica las tres ramas de la regla de idempotencia y de ack manual.
 */
@ExtendWith(MockitoExtension.class)
class TareaSolicitadaConsumerTest {

    @Mock
    private ProcesadorTareaService procesadorTareaService;

    @Mock
    private EventoProcesadoJpaRepository eventoProcesadoRepository;

    @Mock
    private Acknowledgment acknowledgment;

    @Test
    void eventoYaProcesadoSeIgnoraYSeHaceAck() {
        TareaSolicitadaConsumer consumer =
                new TareaSolicitadaConsumer(procesadorTareaService, eventoProcesadoRepository);
        TareaSolicitadaEvent event = evento();
        when(eventoProcesadoRepository.existsById(event.id())).thenReturn(true);

        consumer.handle(record(event), acknowledgment);

        verify(procesadorTareaService, never()).procesar(anyString(), anyString());
        verify(acknowledgment).acknowledge();
    }

    @Test
    void eventoNuevoProcesaTareaRegistraDedupYHaceAck() {
        TareaSolicitadaConsumer consumer =
                new TareaSolicitadaConsumer(procesadorTareaService, eventoProcesadoRepository);
        TareaSolicitadaEvent event = evento();
        when(eventoProcesadoRepository.existsById(event.id())).thenReturn(false);

        consumer.handle(record(event), acknowledgment);

        verify(procesadorTareaService).procesar(event.data().recursoId(), event.data().accion());
        verify(eventoProcesadoRepository).save(any());
        verify(acknowledgment).acknowledge();
    }

    @Test
    void errorAlProcesarNoHaceAckYPropagaLaExcepcion() {
        TareaSolicitadaConsumer consumer =
                new TareaSolicitadaConsumer(procesadorTareaService, eventoProcesadoRepository);
        TareaSolicitadaEvent event = evento();
        when(eventoProcesadoRepository.existsById(event.id())).thenReturn(false);
        doThrow(new TareaNoProcesableException("recurso-inexistente"))
                .when(procesadorTareaService).procesar(anyString(), anyString());

        // No se captura la excepción en el consumer — sube para que DefaultErrorHandler
        // decida reintento o DLQ (LIN-BUS-001 §8.6). Si aquí se hiciera ack, el mensaje se
        // perdería en silencio.
        assertThatThrownBy(() -> consumer.handle(record(event), acknowledgment))
                .isInstanceOf(TareaNoProcesableException.class);

        verify(acknowledgment, never()).acknowledge();
        verify(eventoProcesadoRepository, never()).save(any());
    }

    private TareaSolicitadaEvent evento() {
        return new TareaSolicitadaEvent(
                "1.0",
                UUID.randomUUID().toString(),
                "/onp/sistema",
                "pe.gob.onp.sistema.dominio.tarea-solicitada",
                "2026-07-09T10:30:00Z",
                "application/json",
                new TareaSolicitadaData(UUID.randomUUID().toString(), "activar"));
    }

    private ConsumerRecord<String, TareaSolicitadaEvent> record(TareaSolicitadaEvent event) {
        return new ConsumerRecord<>("sistema.dominio.tarea-solicitada", 0, 0L, "clave-particion", event);
    }
}
