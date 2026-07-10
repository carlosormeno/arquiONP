package pe.gob.onp.template.afiliacion.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio técnico de Spring Data — solo lo usa {@link AfiliadoJpaAdapter}. */
interface AfiliadoJpaRepository extends JpaRepository<AfiliadoEntity, UUID> {

    Optional<AfiliadoEntity> findByDni(String dni);
}
