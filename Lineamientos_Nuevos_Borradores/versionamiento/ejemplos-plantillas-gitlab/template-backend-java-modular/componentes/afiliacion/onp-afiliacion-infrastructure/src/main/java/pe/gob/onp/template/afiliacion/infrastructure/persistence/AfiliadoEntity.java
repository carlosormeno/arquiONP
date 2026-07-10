package pe.gob.onp.template.afiliacion.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * Entidad JPA — vive exclusivamente en -infrastructure. Deliberadamente distinta de
 * {@code Afiliado} (dominio puro): esta clase conoce columnas y tipos de Oracle,
 * el dominio no (LIN-DEV-JAVA-001 §14.1, LIN-DIS-001 §3.3).
 */
@Entity
@Table(name = "TB_AFILIADO")
public class AfiliadoEntity {

    @Id
    @Column(name = "AFI_ID")
    private UUID id;

    @Column(name = "AFI_DNI", nullable = false, unique = true, length = 8)
    private String dni;

    @Column(name = "AFI_NOMBRE_COMPLETO", nullable = false)
    private String nombreCompleto;

    @Enumerated(EnumType.STRING)
    @Column(name = "AFI_ESTADO", nullable = false, length = 20)
    private String estado;

    @Column(name = "AFI_FECHA_REGISTRO", nullable = false)
    private Instant fechaRegistro;

    protected AfiliadoEntity() {
        // Requerido por JPA
    }

    public AfiliadoEntity(UUID id, String dni, String nombreCompleto, String estado, Instant fechaRegistro) {
        this.id = id;
        this.dni = dni;
        this.nombreCompleto = nombreCompleto;
        this.estado = estado;
        this.fechaRegistro = fechaRegistro;
    }

    public UUID getId() {
        return id;
    }

    public String getDni() {
        return dni;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getEstado() {
        return estado;
    }

    public Instant getFechaRegistro() {
        return fechaRegistro;
    }
}
