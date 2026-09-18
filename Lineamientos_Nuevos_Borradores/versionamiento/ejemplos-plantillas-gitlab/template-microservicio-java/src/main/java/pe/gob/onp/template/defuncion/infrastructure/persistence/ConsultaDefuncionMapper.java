package pe.gob.onp.template.defuncion.infrastructure.persistence;

import org.springframework.stereotype.Component;
import pe.gob.onp.template.defuncion.domain.model.ConsultaDefuncion;
import pe.gob.onp.template.defuncion.domain.model.ConsultaDefuncionId;
import pe.gob.onp.template.defuncion.domain.model.Dni;
import pe.gob.onp.template.defuncion.domain.model.EstadoDefuncion;

/** Traduce entre el Agregado de dominio puro y la Entidad JPA (LIN-DEV-JAVA-001 §8.4.2). */
@Component
class ConsultaDefuncionMapper {

    ConsultaDefuncion toDomain(ConsultaDefuncionEntity entity) {
        return ConsultaDefuncion.reconstruir(
                new ConsultaDefuncionId(entity.getId()),
                new Dni(entity.getDni()),
                EstadoDefuncion.valueOf(entity.getEstado()),
                entity.getFechaConsulta());
    }

    ConsultaDefuncionEntity toEntity(ConsultaDefuncion consulta) {
        return new ConsultaDefuncionEntity(
                consulta.getId().valor(),
                consulta.getDni().valor(),
                consulta.getEstado().name(),
                consulta.getFechaConsulta());
    }
}
