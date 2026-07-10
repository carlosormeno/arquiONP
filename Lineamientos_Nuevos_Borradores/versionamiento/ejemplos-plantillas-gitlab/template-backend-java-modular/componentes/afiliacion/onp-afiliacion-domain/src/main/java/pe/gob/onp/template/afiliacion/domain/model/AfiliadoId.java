package pe.gob.onp.template.afiliacion.domain.model;

import java.util.Objects;
import java.util.UUID;

/** Identidad del Agregado Raíz {@link Afiliado}. */
public record AfiliadoId(UUID valor) {

    public AfiliadoId {
        Objects.requireNonNull(valor, "El identificador del afiliado no puede ser nulo");
    }

    public static AfiliadoId nuevo() {
        return new AfiliadoId(UUID.randomUUID());
    }
}
