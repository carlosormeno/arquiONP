package pe.gob.onp.template.defuncion.domain.model;

import java.util.Objects;
import java.util.UUID;

/** Identidad del Agregado Raíz {@link ConsultaDefuncion}. */
public record ConsultaDefuncionId(UUID valor) {

    public ConsultaDefuncionId {
        Objects.requireNonNull(valor, "El identificador de la consulta de defunción no puede ser nulo");
    }

    public static ConsultaDefuncionId nuevo() {
        return new ConsultaDefuncionId(UUID.randomUUID());
    }
}
