package pe.gob.onp.template.afiliacion.domain.port.in;

import pe.gob.onp.common.domain.model.Dni;
import pe.gob.onp.template.afiliacion.domain.model.Afiliado;

/**
 * Puerto de entrada (caso de uso). El dominio define QUÉ se puede pedir — no sabe si
 * la petición viene de un {@code @RestController}, un consumidor Kafka o una prueba.
 * La firma usa exclusivamente tipos de dominio (nunca DTOs de application o api),
 * de forma que este módulo -domain no necesite depender de -application ni de -api.
 */
public interface RegistrarAfiliacionUseCase {

    Afiliado registrar(Dni dni, String nombreCompleto);
}
