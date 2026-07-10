package pe.gob.onp.template.afiliacion.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** DTO contractual de entrada REST — documentado en OpenAPI (LIN-API-REST-001). */
public record RegistrarAfiliacionRequest(

        @NotBlank(message = "dni es obligatorio")
        @Pattern(regexp = "\\d{8}", message = "dni debe contener 8 dígitos numéricos")
        String dni,

        @NotBlank(message = "nombreCompleto es obligatorio")
        String nombreCompleto) {
}
