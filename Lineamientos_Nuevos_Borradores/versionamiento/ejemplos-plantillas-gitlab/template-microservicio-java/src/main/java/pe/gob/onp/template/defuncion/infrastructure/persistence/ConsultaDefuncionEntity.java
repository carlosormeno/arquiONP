package pe.gob.onp.template.defuncion.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * Entidad JPA — vive exclusivamente en {@code infrastructure.persistence}. Deliberadamente
 * distinta de {@code ConsultaDefuncion} (dominio puro): esta clase conoce columnas y tipos de
 * Oracle, el dominio no (LIN-DEV-JAVA-001 §14.1, LIN-DIS-001 §3.3).
 */
@Entity
@Table(name = "TB_CONSULTA_DEFUNCION")
public class ConsultaDefuncionEntity {

    @Id
    @Column(name = "CDF_ID")
    private UUID id;

    @Column(name = "CDF_DNI", nullable = false, length = 8)
    private String dni;

    @Enumerated(EnumType.STRING)
    @Column(name = "CDF_ESTADO", nullable = false, length = 20)
    private String estado;

    @Column(name = "CDF_FECHA_CONSULTA", nullable = false)
    private Instant fechaConsulta;

    protected ConsultaDefuncionEntity() {
        // Requerido por JPA
    }

    public ConsultaDefuncionEntity(UUID id, String dni, String estado, Instant fechaConsulta) {
        this.id = id;
        this.dni = dni;
        this.estado = estado;
        this.fechaConsulta = fechaConsulta;
    }

    public UUID getId() {
        return id;
    }

    public String getDni() {
        return dni;
    }

    public String getEstado() {
        return estado;
    }

    public Instant getFechaConsulta() {
        return fechaConsulta;
    }
}
