package pe.gob.onp.template.afiliacion.domain.model;

import java.time.Instant;
import java.util.Objects;
import pe.gob.onp.common.domain.model.Dni;

/**
 * Agregado Raíz del componente de negocio {@code afiliacion}.
 * Encapsula las reglas de negocio y las transiciones de estado — nadie externo
 * modifica su estado sin pasar por sus métodos (LIN-DIS-001 §3.2). NO es una
 * entidad JPA: esta clase vive en el anillo interior hexagonal y no conoce
 * persistencia, HTTP ni ningún framework.
 */
public class Afiliado {

    private final AfiliadoId id;
    private final Dni dni;
    private final String nombreCompleto;
    private EstadoAfiliado estado;
    private final Instant fechaRegistro;

    private Afiliado(AfiliadoId id, Dni dni, String nombreCompleto, EstadoAfiliado estado, Instant fechaRegistro) {
        this.id = Objects.requireNonNull(id);
        this.dni = Objects.requireNonNull(dni);
        this.nombreCompleto = requireNombreValido(nombreCompleto);
        this.estado = Objects.requireNonNull(estado);
        this.fechaRegistro = Objects.requireNonNull(fechaRegistro);
    }

    /** Factory de creación — único punto de entrada para registrar un afiliado nuevo. */
    public static Afiliado registrar(Dni dni, String nombreCompleto) {
        return new Afiliado(AfiliadoId.nuevo(), dni, nombreCompleto, EstadoAfiliado.REGISTRADO, Instant.now());
    }

    /** Reconstrucción desde persistencia — usada exclusivamente por el Mapper de infraestructura. */
    public static Afiliado reconstruir(
            AfiliadoId id, Dni dni, String nombreCompleto, EstadoAfiliado estado, Instant fechaRegistro) {
        return new Afiliado(id, dni, nombreCompleto, estado, fechaRegistro);
    }

    public void activar() {
        this.estado = estado.activar();
    }

    public void suspender() {
        this.estado = estado.suspender();
    }

    private static String requireNombreValido(String nombreCompleto) {
        if (nombreCompleto == null || nombreCompleto.isBlank()) {
            throw new IllegalArgumentException("El nombre completo del afiliado es obligatorio");
        }
        return nombreCompleto;
    }

    public AfiliadoId getId() {
        return id;
    }

    public Dni getDni() {
        return dni;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public EstadoAfiliado getEstado() {
        return estado;
    }

    public Instant getFechaRegistro() {
        return fechaRegistro;
    }
}
