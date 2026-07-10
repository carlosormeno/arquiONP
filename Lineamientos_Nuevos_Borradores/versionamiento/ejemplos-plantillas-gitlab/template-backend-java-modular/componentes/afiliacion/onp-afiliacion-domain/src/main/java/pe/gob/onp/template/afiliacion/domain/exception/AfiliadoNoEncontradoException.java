package pe.gob.onp.template.afiliacion.domain.exception;

import pe.gob.onp.common.domain.exception.RecursoNoEncontradoException;
import pe.gob.onp.template.afiliacion.domain.model.AfiliadoId;

public class AfiliadoNoEncontradoException extends RecursoNoEncontradoException {

    public AfiliadoNoEncontradoException(String dni) {
        super("No existe afiliado registrado con DNI: " + dni);
    }

    public AfiliadoNoEncontradoException(AfiliadoId id) {
        super("No existe afiliado registrado con id: " + id.valor());
    }
}
