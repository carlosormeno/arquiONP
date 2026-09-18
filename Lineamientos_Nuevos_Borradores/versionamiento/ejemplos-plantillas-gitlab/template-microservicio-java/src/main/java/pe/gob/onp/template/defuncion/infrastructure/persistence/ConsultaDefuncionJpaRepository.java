package pe.gob.onp.template.defuncion.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio técnico Spring Data JPA — solo existe en {@code infrastructure.persistence}. */
interface ConsultaDefuncionJpaRepository extends JpaRepository<ConsultaDefuncionEntity, UUID> {

    List<ConsultaDefuncionEntity> findByDniOrderByFechaConsultaDesc(String dni, Pageable pageable);
}
