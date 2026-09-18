package pe.gob.onp.template.worker.messaging.dedup;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Registro técnico de idempotencia (LIN-BUS-001 §8.4): guarda el {@code id} de cada evento
 * CloudEvents ya procesado por {@link pe.gob.onp.template.worker.messaging.TareaSolicitadaConsumer}.
 * No es un concepto de negocio — es infraestructura de mensajería, por eso vive en
 * {@code messaging.dedup} y no en {@code batch} (que tiene su propia tabla,
 * {@code TB_REGISTRO_PENDIENTE}, sin relación con esta).
 *
 * <p>Esquema creado por Flyway — ver {@code db/migration/} (LIN-BD-ORA-001 §8.4). Nunca por
 * {@code hibernate.ddl-auto} (fijado en {@code validate}, ver application.yml).
 */
@Entity
@Table(name = "TB_EVENTO_PROCESADO")
public class EventoProcesadoEntity {

    @Id
    @Column(name = "EVE_ID", length = 36)
    private String eventoId;

    @Column(name = "EVE_PROCESADO_EN", nullable = false)
    private Instant procesadoEn;

    protected EventoProcesadoEntity() {
        // Requerido por JPA
    }

    public EventoProcesadoEntity(String eventoId, Instant procesadoEn) {
        this.eventoId = eventoId;
        this.procesadoEn = procesadoEn;
    }

    public String getEventoId() {
        return eventoId;
    }

    public Instant getProcesadoEn() {
        return procesadoEn;
    }
}
