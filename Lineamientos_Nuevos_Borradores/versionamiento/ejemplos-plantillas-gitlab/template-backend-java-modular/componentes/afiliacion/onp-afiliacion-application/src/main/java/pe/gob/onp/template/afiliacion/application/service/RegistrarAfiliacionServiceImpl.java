package pe.gob.onp.template.afiliacion.application.service;

import pe.gob.onp.common.domain.model.Dni;
import pe.gob.onp.template.afiliacion.domain.exception.AfiliadoYaRegistradoException;
import pe.gob.onp.template.afiliacion.domain.model.Afiliado;
import pe.gob.onp.template.afiliacion.domain.port.in.RegistrarAfiliacionUseCase;
import pe.gob.onp.template.afiliacion.domain.port.out.AfiliadoRepository;

/**
 * Implementación pura del caso de uso — POJO sin {@code @Service}, sin {@code @Transactional}
 * ni ningún estereotipo de Spring (LIN-DEV-JAVA-001 §14.1). Recibe sus puertos de salida
 * por inyección de constructor. El cableado hacia el contenedor de Spring (incluyendo la
 * demarcación transaccional) ocurre exclusivamente en la capa -infrastructure, en la clase
 * {@code @Configuration} que declara este servicio como {@code @Bean}.
 */
public class RegistrarAfiliacionServiceImpl implements RegistrarAfiliacionUseCase {

    private final AfiliadoRepository afiliadoRepository;

    public RegistrarAfiliacionServiceImpl(AfiliadoRepository afiliadoRepository) {
        this.afiliadoRepository = afiliadoRepository;
    }

    @Override
    public Afiliado registrar(Dni dni, String nombreCompleto) {
        afiliadoRepository.buscarPorDni(dni).ifPresent(existente -> {
            throw new AfiliadoYaRegistradoException(dni.valor());
        });

        Afiliado afiliado = Afiliado.registrar(dni, nombreCompleto);
        return afiliadoRepository.guardar(afiliado);
    }
}
