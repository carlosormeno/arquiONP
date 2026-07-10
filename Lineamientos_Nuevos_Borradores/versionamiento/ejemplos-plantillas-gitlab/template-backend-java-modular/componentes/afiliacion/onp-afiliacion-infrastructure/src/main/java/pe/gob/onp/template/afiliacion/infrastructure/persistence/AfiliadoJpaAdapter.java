package pe.gob.onp.template.afiliacion.infrastructure.persistence;

import java.util.Optional;
import org.springframework.stereotype.Repository;
import pe.gob.onp.common.domain.model.Dni;
import pe.gob.onp.template.afiliacion.domain.model.Afiliado;
import pe.gob.onp.template.afiliacion.domain.model.AfiliadoId;
import pe.gob.onp.template.afiliacion.domain.port.out.AfiliadoRepository;

/**
 * Adapter de salida — implementa el puerto {@link AfiliadoRepository} del dominio
 * usando Spring Data JPA sobre Oracle. El dominio nunca ve esta clase ni sus tipos.
 */
@Repository
class AfiliadoJpaAdapter implements AfiliadoRepository {

    private final AfiliadoJpaRepository jpaRepository;
    private final AfiliadoMapper mapper;

    AfiliadoJpaAdapter(AfiliadoJpaRepository jpaRepository, AfiliadoMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Afiliado> buscarPorDni(Dni dni) {
        return jpaRepository.findByDni(dni.valor()).map(mapper::toDomain);
    }

    @Override
    public Optional<Afiliado> buscarPorId(AfiliadoId id) {
        return jpaRepository.findById(id.valor()).map(mapper::toDomain);
    }

    @Override
    public Afiliado guardar(Afiliado afiliado) {
        AfiliadoEntity guardado = jpaRepository.save(mapper.toEntity(afiliado));
        return mapper.toDomain(guardado);
    }
}
