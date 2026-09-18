package pe.gob.onp.template.defuncion.domain.port.in;

import pe.gob.onp.template.defuncion.domain.model.ConsultaDefuncion;
import pe.gob.onp.template.defuncion.domain.model.Dni;

/**
 * Puerto de entrada (caso de uso). El dominio define QUÉ se puede pedir — no sabe si la petición
 * viene de un {@code @RestController}, un job batch o una prueba. La firma usa exclusivamente
 * tipos de dominio (nunca DTOs de infraestructura), de forma que {@code domain} no dependa de
 * {@code infrastructure} (LIN-DEV-JAVA-001 §14.1).
 */
public interface ConsultarEstadoDefuncionUseCase {

    ConsultaDefuncion consultar(Dni dni);
}
