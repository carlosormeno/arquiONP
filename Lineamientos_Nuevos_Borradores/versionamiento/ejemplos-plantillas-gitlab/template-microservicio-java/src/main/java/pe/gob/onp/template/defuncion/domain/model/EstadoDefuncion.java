package pe.gob.onp.template.defuncion.domain.model;

/**
 * Resultado de la verificación de estado vital de una persona en el Registro Civil (RENIEC).
 * Es el dato de negocio central de este microservicio de consulta/validación — usado, por
 * ejemplo, como insumo previo a la autorización de pago de una pensión (suspensión por
 * fallecimiento no reportado). No es una máquina de estados con transiciones propias (a
 * diferencia de {@code EstadoAfiliado} en `template-backend-java-modular`): es un resultado de
 * consulta de solo lectura frente a un sistema externo, se recalcula en cada consulta.
 */
public enum EstadoDefuncion {

    VIVO,
    FALLECIDO,
    NO_DETERMINADO
}
