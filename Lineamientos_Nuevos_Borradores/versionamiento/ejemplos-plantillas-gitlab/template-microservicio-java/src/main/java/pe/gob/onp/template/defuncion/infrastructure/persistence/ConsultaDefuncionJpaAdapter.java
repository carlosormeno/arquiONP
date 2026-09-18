package pe.gob.onp.template.defuncion.infrastructure.persistence;

import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import pe.gob.onp.template.defuncion.domain.model.ConsultaDefuncion;
import pe.gob.onp.template.defuncion.domain.model.Dni;
import pe.gob.onp.template.defuncion.domain.port.out.ConsultaDefuncionRepository;

/**
 * Adapter de salida — implementa el puerto {@link ConsultaDefuncionRepository} del dominio
 * usando Spring Data JPA sobre Oracle. El dominio nunca ve esta clase ni sus tipos (LIN-DEV-JAVA-001
 * §14.1, ejemplo oficial de Port/Adapter).
 */
@Repository
class ConsultaDefuncionJpaAdapter implements ConsultaDefuncionRepository {

    private final ConsultaDefuncionJpaRepository jpaRepository;
    private final ConsultaDefuncionMapper mapper;

    ConsultaDefuncionJpaAdapter(ConsultaDefuncionJpaRepository jpaRepository, ConsultaDefuncionMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public ConsultaDefuncion guardar(ConsultaDefuncion consulta) {
        ConsultaDefuncionEntity guardado = jpaRepository.save(mapper.toEntity(consulta));
        return mapper.toDomain(guardado);
    }

    @Override
    public Optional<ConsultaDefuncion> buscarUltimaPorDni(Dni dni) {
        return jpaRepository.findByDniOrderByFechaConsultaDesc(dni.valor(), PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .map(mapper::toDomain);
    }
}
