package pe.gob.onp.template.afiliacion.messaging.dedup;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EventoProcesadoJpaRepository extends JpaRepository<EventoProcesadoEntity, String> {
}
