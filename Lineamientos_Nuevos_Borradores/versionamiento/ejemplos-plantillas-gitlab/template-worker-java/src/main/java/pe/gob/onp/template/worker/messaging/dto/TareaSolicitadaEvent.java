package pe.gob.onp.template.worker.messaging.dto;

/**
 * Envelope conforme a CloudEvents v1.0 (LIN-BUS-001 §5.2). Ejemplo neutro y genérico — NO es
 * una réplica del evento de negocio "afiliación" de {@code template-backend-java-modular}; solo
 * reutiliza su misma forma de envelope para que el equipo que adopte esta plantilla sepa qué
 * contrato de mensaje esperar de un tópico real.
 *
 * <p>El campo {@code data} solo debe llevar identificadores internos — nunca PII (Política No
 * PII, LIN-BUS-001 §5.3). Reemplazar {@code sistema.dominio.tarea-solicitada} por el tópico real
 * del sistema (ver {@link pe.gob.onp.template.worker.messaging.TareaSolicitadaConsumer}).
 */
public record TareaSolicitadaEvent(
        String specversion,
        String id,
        String source,
        String type,
        String time,
        String datacontenttype,
        TareaSolicitadaData data) {

    public record TareaSolicitadaData(String recursoId, String accion) {
    }
}
