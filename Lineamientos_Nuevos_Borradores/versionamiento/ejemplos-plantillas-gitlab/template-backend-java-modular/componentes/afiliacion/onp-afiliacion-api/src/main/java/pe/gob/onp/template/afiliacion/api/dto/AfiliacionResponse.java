package pe.gob.onp.template.afiliacion.api.dto;

import pe.gob.onp.template.afiliacion.application.dto.AfiliacionResult;

/** DTO contractual de salida REST — versionado con Full Compatibility (LIN-ARQ-001 §4.1). */
public record AfiliacionResponse(String afiliadoId, String dni, String nombreCompleto, String estado) {

    public static AfiliacionResponse from(AfiliacionResult result) {
        return new AfiliacionResponse(result.afiliadoId(), result.dni(), result.nombreCompleto(), result.estado());
    }
}
