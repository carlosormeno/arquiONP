package pe.gob.onp.template.defuncion.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Agregado Raíz — único Aggregate Root de este microservicio acotado (LIN-DIS-001 §3.2). Registra
 * el resultado de una verificación puntual de estado de defunción de una persona natural contra
 * el Registro Civil (RENIEC), consultado a través del puerto de salida
 * {@link pe.gob.onp.template.defuncion.domain.port.out.RegistroCivilDefuncionClient}.
 *
 * <p>NO es una entidad JPA: vive en el anillo interior hexagonal (`domain/`) y no conoce
 * persistencia, HTTP ni ningún framework (LIN-DEV-JAVA-001 §14.1, "Regla de pureza hexagonal").</p>
 */
public class ConsultaDefuncion {

    private final ConsultaDefuncionId id;
    private final Dni dni;
    private final EstadoDefuncion estado;
    private final Instant fechaConsulta;

    private ConsultaDefuncion(ConsultaDefuncionId id, Dni dni, EstadoDefuncion estado, Instant fechaConsulta) {
        this.id = Objects.requireNonNull(id);
        this.dni = Objects.requireNonNull(dni);
        this.estado = Objects.requireNonNull(estado);
        this.fechaConsulta = Objects.requireNonNull(fechaConsulta);
    }

    /** Factory de creación — registra el resultado de una consulta recién efectuada. */
    public static ConsultaDefuncion registrar(Dni dni, EstadoDefuncion estado) {
        return new ConsultaDefuncion(ConsultaDefuncionId.nuevo(), dni, estado, Instant.now());
    }

    /** Reconstrucción desde persistencia — usada exclusivamente por el Mapper de infraestructura. */
    public static ConsultaDefuncion reconstruir(
            ConsultaDefuncionId id, Dni dni, EstadoDefuncion estado, Instant fechaConsulta) {
        return new ConsultaDefuncion(id, dni, estado, fechaConsulta);
    }

    /** Regla de negocio: una consulta se considera vigente (no requiere reconsulta) por 24 horas. */
    public boolean esVigente(Instant ahora) {
        return fechaConsulta.plusSeconds(24 * 3600).isAfter(ahora);
    }

    public ConsultaDefuncionId getId() {
        return id;
    }

    public Dni getDni() {
        return dni;
    }

    public EstadoDefuncion getEstado() {
        return estado;
    }

    public Instant getFechaConsulta() {
        return fechaConsulta;
    }
}
