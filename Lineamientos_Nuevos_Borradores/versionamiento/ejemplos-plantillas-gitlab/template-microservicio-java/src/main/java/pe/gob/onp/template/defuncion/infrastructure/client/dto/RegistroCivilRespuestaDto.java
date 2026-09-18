package pe.gob.onp.template.defuncion.infrastructure.client.dto;

/**
 * Forma del contrato JSON expuesto por el servicio externo de Registro Civil (RENIEC). Vive
 * exclusivamente en {@code infrastructure.client} — el dominio nunca ve este tipo, solo el
 * resultado ya traducido a {@link pe.gob.onp.template.defuncion.domain.model.EstadoDefuncion}
 * (ver {@code RegistroCivilHttpAdapter#mapEstado}).
 */
public record RegistroCivilRespuestaDto(String dni, String estadoCivilVital, String fechaDefuncion) {
}
