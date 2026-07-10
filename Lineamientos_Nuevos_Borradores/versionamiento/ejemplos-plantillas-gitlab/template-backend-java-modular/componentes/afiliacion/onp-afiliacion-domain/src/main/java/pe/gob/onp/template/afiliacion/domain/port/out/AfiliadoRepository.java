package pe.gob.onp.template.afiliacion.domain.port.out;

import java.util.Optional;
import pe.gob.onp.common.domain.model.Dni;
import pe.gob.onp.template.afiliacion.domain.model.Afiliado;
import pe.gob.onp.template.afiliacion.domain.model.AfiliadoId;

/**
 * Puerto de salida. El dominio define CÓMO necesita persistir/consultar afiliados,
 * sin saber si la implementación usa Oracle, un mock en memoria o cualquier otro
 * mecanismo — eso lo decide el Adapter en -infrastructure.
 */
public interface AfiliadoRepository {

    Optional<Afiliado> buscarPorDni(Dni dni);

    Optional<Afiliado> buscarPorId(AfiliadoId id);

    Afiliado guardar(Afiliado afiliado);
}
