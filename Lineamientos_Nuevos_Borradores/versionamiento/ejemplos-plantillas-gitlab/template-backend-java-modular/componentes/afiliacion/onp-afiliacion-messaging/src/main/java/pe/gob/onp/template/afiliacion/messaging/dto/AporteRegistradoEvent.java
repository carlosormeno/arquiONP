package pe.gob.onp.template.afiliacion.messaging.dto;

/**
 * Envelope conforme a CloudEvents v1.0 (LIN-BUS-001 §5.2). Publicado por el
 * contexto {@code aportes} en el tópico {@code aportes.registro.aporte-registrado};
 * {@code afiliacion} es uno de sus consumidores.
 *
 * <p>El campo {@code data} solo lleva identificadores internos — nunca el DNI ni
 * ningún otro dato personal (Política No PII, LIN-BUS-001 §5.3). Si algún
 * consumidor necesitara datos personales, los obtiene con una consulta REST al
 * servicio origen, no leyéndolos del evento.
 */
public record AporteRegistradoEvent(
        String specversion,
        String id,
        String source,
        String type,
        String time,
        String datacontenttype,
        AporteRegistradoData data) {

    public record AporteRegistradoData(String afiliadoId, String aporteId) {
    }
}
