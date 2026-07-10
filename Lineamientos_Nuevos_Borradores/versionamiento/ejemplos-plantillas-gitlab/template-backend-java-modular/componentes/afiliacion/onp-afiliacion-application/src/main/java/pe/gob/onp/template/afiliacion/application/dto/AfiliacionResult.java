package pe.gob.onp.template.afiliacion.application.dto;

import pe.gob.onp.template.afiliacion.domain.model.Afiliado;

/**
 * Proyección interna de solo lectura del caso de uso — deliberadamente distinta del
 * DTO contractual HTTP en -api (que además debe respetar compatibilidad de versión
 * OpenAPI). Desacopla el contrato externo publicado de la forma interna que usan
 * los casos de uso, para poder cambiar uno sin romper el otro.
 */
public record AfiliacionResult(String afiliadoId, String dni, String nombreCompleto, String estado) {

    public static AfiliacionResult from(Afiliado afiliado) {
        return new AfiliacionResult(
                afiliado.getId().valor().toString(),
                afiliado.getDni().valor(),
                afiliado.getNombreCompleto(),
                afiliado.getEstado().name());
    }
}
