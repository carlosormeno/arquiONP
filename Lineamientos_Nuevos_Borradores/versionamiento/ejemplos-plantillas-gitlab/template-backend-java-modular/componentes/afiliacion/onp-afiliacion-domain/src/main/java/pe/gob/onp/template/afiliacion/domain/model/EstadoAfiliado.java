package pe.gob.onp.template.afiliacion.domain.model;

/**
 * Ciclo de vida del afiliado con transiciones controladas (patrón State, LIN-DEV-JAVA-001 §8.3.4).
 * Ninguna capa externa asigna el estado directamente — las transiciones ocurren a través
 * de los métodos de negocio del Agregado Raíz {@link Afiliado}.
 */
public enum EstadoAfiliado {

    REGISTRADO {
        @Override
        public EstadoAfiliado activar() {
            return ACTIVO;
        }
    },
    ACTIVO {
        @Override
        public EstadoAfiliado suspender() {
            return SUSPENDIDO;
        }
    },
    SUSPENDIDO {
        @Override
        public EstadoAfiliado activar() {
            return ACTIVO;
        }
    };

    public EstadoAfiliado activar() {
        throw new IllegalStateException("Transición 'activar' no válida desde: " + this);
    }

    public EstadoAfiliado suspender() {
        throw new IllegalStateException("Transición 'suspender' no válida desde: " + this);
    }
}
