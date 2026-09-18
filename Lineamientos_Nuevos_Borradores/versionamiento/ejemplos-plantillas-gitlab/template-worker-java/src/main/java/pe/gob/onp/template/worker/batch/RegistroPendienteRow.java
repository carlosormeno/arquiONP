package pe.gob.onp.template.worker.batch;

import java.time.Instant;

/**
 * Fila de {@code TB_REGISTRO_PENDIENTE} representada como {@code record} liviano — deliberadamente
 * NO es una entidad JPA ({@code @Entity}). El patrón Table Module (LIN-DIS-001 §4.1) exige operar
 * sobre la colección completa en memoria sin instanciar miles de objetos de dominio; un
 * {@code record} de datos planos más una única lectura/escritura por lote (ver
 * {@link RegistroPendienteTableModule} y {@link ActualizacionMasivaJobRunner}) cumple eso mejor
 * que un grafo de entidades JPA gestionado fila por fila.
 */
public record RegistroPendienteRow(Long id, String estado, Instant fechaCreacion) {

    public static final String ESTADO_PENDIENTE = "PENDIENTE";
    public static final String ESTADO_EXPIRADO = "EXPIRADO";

    /** Copia esta fila con un nuevo estado — el record original es inmutable. */
    public RegistroPendienteRow conEstado(String nuevoEstado) {
        return new RegistroPendienteRow(id, nuevoEstado, fechaCreacion);
    }
}
