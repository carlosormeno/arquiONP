package pe.gob.onp.template.defuncion.infrastructure.web.dto;

import pe.gob.onp.template.defuncion.domain.model.ConsultaDefuncion;

/** DTO contractual de salida REST — versionado con Full Compatibility (LIN-ARQ-001 §4.1). */
public record ConsultaDefuncionResponse(String dni, String estado, String fechaConsulta) {

    public static ConsultaDefuncionResponse from(ConsultaDefuncion consulta) {
        return new ConsultaDefuncionResponse(
                consulta.getDni().valor(),
                consulta.getEstado().name(),
                consulta.getFechaConsulta().toString());
    }
}
