package pe.gob.onp.template.afiliacion.messaging;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
import pe.gob.onp.template.afiliacion.domain.exception.AfiliadoNoEncontradoException;
import pe.gob.onp.template.afiliacion.domain.model.AfiliadoId;
import pe.gob.onp.template.afiliacion.domain.port.in.ActivarAfiliacionPorAporteUseCase;
import pe.gob.onp.template.afiliacion.messaging.dedup.EventoProcesadoJpaRepository;
import pe.gob.onp.template.afiliacion.messaging.dto.AporteRegistradoEvent;
import pe.gob.onp.template.afiliacion.messaging.dto.AporteRegistradoEvent.AporteRegistradoData;

/**
 * Unitaria con Mockito — sin Kafka real, sin contexto Spring (LIN-TEST-001 §4.2).
 * Verifica las tres ramas de la regla de idempotencia y de ack manual.
 */
@ExtendWith(MockitoExtension.class)
class AporteRegistradoConsumerTest {

    @Mock
    private ActivarAfiliacionPorAporteUseCase activarAfiliacionUseCase;

    @Mock
    private EventoProcesadoJpaRepository eventoProcesadoRepository;

    @Mock
    private Acknowledgment acknowledgment;

    @Test
    void eventoYaProcesadoSeIgnoraYSeHaceAck() {
        AporteRegistradoConsumer consumer =
                new AporteRegistradoConsumer(activarAfiliacionUseCase, eventoProcesadoRepository);
        AporteRegistradoEvent event = evento(UUID.randomUUID());
        when(eventoProcesadoRepository.existsById(event.id())).thenReturn(true);

        consumer.handle(record(event), acknowledgment);

        verify(activarAfiliacionUseCase, never()).activarPorAporte(any());
        verify(acknowledgment).acknowledge();
    }

    @Test
    void eventoNuevoActivaAfiliacionRegistraDedupYHaceAck() {
        AporteRegistradoConsumer consumer =
                new AporteRegistradoConsumer(activarAfiliacionUseCase, eventoProcesadoRepository);
        AporteRegistradoEvent event = evento(UUID.randomUUID());
        when(eventoProcesadoRepository.existsById(event.id())).thenReturn(false);

        consumer.handle(record(event), acknowledgment);

        verify(activarAfiliacionUseCase).activarPorAporte(any(AfiliadoId.class));
        verify(eventoProcesadoRepository).save(any());
        verify(acknowledgment).acknowledge();
    }

    @Test
    void errorAlActivarNoHaceAckYPropagaLaExcepcion() {
        AporteRegistradoConsumer consumer =
                new AporteRegistradoConsumer(activarAfiliacionUseCase, eventoProcesadoRepository);
        AporteRegistradoEvent event = evento(UUID.randomUUID());
        when(eventoProcesadoRepository.existsById(event.id())).thenReturn(false);
        doThrow(new AfiliadoNoEncontradoException("dni-inexistente"))
                .when(activarAfiliacionUseCase).activarPorAporte(any());

        // No se captura la excepción en el consumer — sube para que DefaultErrorHandler
        // decida reintento o DLQ (LIN-BUS-001 §8.6). Si aquí se hiciera ack, el mensaje
        // se perdería en silencio, que es justamente el bug que corregimos del ejemplo original.
        assertThatThrownBy(() -> consumer.handle(record(event), acknowledgment))
                .isInstanceOf(AfiliadoNoEncontradoException.class);

        verify(acknowledgment, never()).acknowledge();
        verify(eventoProcesadoRepository, never()).save(any());
    }

    private AporteRegistradoEvent evento(UUID afiliadoId) {
        return new AporteRegistradoEvent(
                "1.0",
                UUID.randomUUID().toString(),
                "/onp/aportes",
                "pe.gob.onp.aportes.registro.aporte-registrado",
                "2026-07-09T10:30:00Z",
                "application/json",
                new AporteRegistradoData(afiliadoId.toString(), UUID.randomUUID().toString()));
    }

    private ConsumerRecord<String, AporteRegistradoEvent> record(AporteRegistradoEvent event) {
        return new ConsumerRecord<>("aportes.registro.aporte-registrado", 0, 0L, "clave-particion", event);
    }
}
