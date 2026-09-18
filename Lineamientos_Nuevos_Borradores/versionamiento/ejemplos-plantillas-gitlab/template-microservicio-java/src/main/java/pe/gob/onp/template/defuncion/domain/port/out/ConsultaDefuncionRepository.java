package pe.gob.onp.template.defuncion.domain.port.out;

import java.util.Optional;
import pe.gob.onp.template.defuncion.domain.model.ConsultaDefuncion;
import pe.gob.onp.template.defuncion.domain.model.Dni;

/**
 * Puerto de salida de persistencia. El dominio define CÓMO necesita guardar/consultar el
 * historial de verificaciones, sin saber si la implementación usa Oracle, un mock en memoria
 * o cualquier otro mecanismo — eso lo decide el Adapter en {@code infrastructure.persistence}.
 */
public interface ConsultaDefuncionRepository {

    ConsultaDefuncion guardar(ConsultaDefuncion consulta);

    Optional<ConsultaDefuncion> buscarUltimaPorDni(Dni dni);
}
