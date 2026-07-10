package pe.gob.onp.template.afiliacion.messaging.dedup;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Registro técnico de idempotencia (LIN-BUS-001 §8.4): guarda el {@code id} de
 * cada evento CloudEvents ya procesado. No es un concepto de negocio de
 * afiliación — es infraestructura de mensajería, por eso vive en -messaging
 * y no comparte tabla ni mapper con {@code AfiliadoEntity}.
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
