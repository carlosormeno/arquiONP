package pe.gob.onp.template.afiliacion.domain.exception;

import pe.gob.onp.common.domain.exception.OnpDomainException;

public class AfiliadoYaRegistradoException extends OnpDomainException {

    public AfiliadoYaRegistradoException(String dni) {
        super("Ya existe un afiliado registrado con DNI: " + dni);
    }
}
