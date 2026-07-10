package pe.gob.onp.template.afiliacion.infrastructure.persistence;

import org.springframework.stereotype.Component;
import pe.gob.onp.common.domain.model.Dni;
import pe.gob.onp.template.afiliacion.domain.model.Afiliado;
import pe.gob.onp.template.afiliacion.domain.model.AfiliadoId;
import pe.gob.onp.template.afiliacion.domain.model.EstadoAfiliado;

/** Traduce entre el Agregado de dominio puro y la Entidad JPA (LIN-DEV-JAVA-001 §8.4.2). */
@Component
class AfiliadoMapper {

    Afiliado toDomain(AfiliadoEntity entity) {
        return Afiliado.reconstruir(
                new AfiliadoId(entity.getId()),
                new Dni(entity.getDni()),
                entity.getNombreCompleto(),
                EstadoAfiliado.valueOf(entity.getEstado()),
                entity.getFechaRegistro());
    }

    AfiliadoEntity toEntity(Afiliado afiliado) {
        return new AfiliadoEntity(
                afiliado.getId().valor(),
                afiliado.getDni().valor(),
                afiliado.getNombreCompleto(),
                afiliado.getEstado().name(),
                afiliado.getFechaRegistro());
    }
}
