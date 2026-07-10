package pe.gob.onp.template.afiliacion.application.command;

import java.util.Objects;

/**
 * Comando de entrada — record inmutable con validación de forma en el constructor
 * compacto (LIN-DEV-JAVA-001 §8.3.3). Lo construye la capa -api a partir del DTO HTTP
 * y sirve como frontera de validación antes de tocar tipos de dominio: aquí se valida
 * "¿el dato tiene la forma correcta?"; las reglas de negocio ("¿es válido en este
 * contexto?") las valida el Agregado Raíz en -domain.
 */
public record RegistrarAfiliacionCommand(String dni, String nombreCompleto) {

    public RegistrarAfiliacionCommand {
        Objects.requireNonNull(dni, "dni requerido");
        Objects.requireNonNull(nombreCompleto, "nombreCompleto requerido");
        if (nombreCompleto.isBlank()) {
            throw new IllegalArgumentException("nombreCompleto no puede estar vacío");
        }
    }
}
