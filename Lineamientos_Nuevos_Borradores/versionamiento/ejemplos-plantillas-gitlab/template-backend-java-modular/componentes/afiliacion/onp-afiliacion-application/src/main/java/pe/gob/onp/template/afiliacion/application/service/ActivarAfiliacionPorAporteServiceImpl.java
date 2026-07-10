package pe.gob.onp.template.afiliacion.application.service;

import pe.gob.onp.template.afiliacion.domain.exception.AfiliadoNoEncontradoException;
import pe.gob.onp.template.afiliacion.domain.model.Afiliado;
import pe.gob.onp.template.afiliacion.domain.model.AfiliadoId;
import pe.gob.onp.template.afiliacion.domain.port.in.ActivarAfiliacionPorAporteUseCase;
import pe.gob.onp.template.afiliacion.domain.port.out.AfiliadoRepository;

/**
 * POJO — mismo patrón que {@link RegistrarAfiliacionServiceImpl}: sin
 * {@code @Service}, sin {@code @Transactional}, inyección por constructor.
 * Se invoca igual desde un Controller HTTP o desde el consumer Kafka de
 * -messaging — el caso de uso no sabe ni le importa quién lo llama.
 */
public class ActivarAfiliacionPorAporteServiceImpl implements ActivarAfiliacionPorAporteUseCase {

    private final AfiliadoRepository afiliadoRepository;

    public ActivarAfiliacionPorAporteServiceImpl(AfiliadoRepository afiliadoRepository) {
        this.afiliadoRepository = afiliadoRepository;
    }

    @Override
    public void activarPorAporte(AfiliadoId afiliadoId) {
        Afiliado afiliado = afiliadoRepository.buscarPorId(afiliadoId)
                .orElseThrow(() -> new AfiliadoNoEncontradoException(afiliadoId));

        afiliado.activar(); // el propio Agregado valida la transición (State, EstadoAfiliado)
        afiliadoRepository.guardar(afiliado);
    }
}
