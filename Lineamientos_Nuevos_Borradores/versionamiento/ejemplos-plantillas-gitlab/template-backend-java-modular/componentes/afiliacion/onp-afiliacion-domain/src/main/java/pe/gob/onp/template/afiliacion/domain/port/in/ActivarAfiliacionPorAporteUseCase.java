package pe.gob.onp.template.afiliacion.domain.port.in;

import pe.gob.onp.template.afiliacion.domain.model.AfiliadoId;

/**
 * Puerto de entrada activado por un evento externo (consumer Kafka en
 * -messaging), no por HTTP — la firma sigue siendo tipos de dominio puros,
 * igual que {@link RegistrarAfiliacionUseCase}. Al dominio no le importa si
 * quien lo invoca es un Controller o un {@code @KafkaListener}.
 */
public interface ActivarAfiliacionPorAporteUseCase {

    void activarPorAporte(AfiliadoId afiliadoId);
}
