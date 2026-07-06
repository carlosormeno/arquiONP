package pe.gob.onp.pensiones.solicitud.application.service;

import io.micrometer.tracing.annotation.NewSpan;
import io.micrometer.tracing.annotation.SpanTag;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.gob.onp.pensiones.solicitud.domain.event.SolicitudPensionRegistradaEvent;
import pe.gob.onp.pensiones.solicitud.domain.exception.AportanteNoEncontradoException;
import pe.gob.onp.pensiones.solicitud.domain.model.Aportante;
import pe.gob.onp.pensiones.solicitud.domain.model.SolicitudPensionResponse;
import pe.gob.onp.pensiones.solicitud.domain.repository.AportanteRepository;
import pe.gob.onp.pensiones.solicitud.domain.service.CalculoPensionVitaliciaDomainService;

import java.math.BigDecimal;

/**
 * Plantilla oficial de referencia para el patrón táctico PD06 (Application Service).
 * <p>
 * **Conexión con Nomenclatura Institucional (Sección 4.2 y Anexo C):**
 * En la convención de nombres de la ONP, el rol arquitectónico de "Application Service"
 * se implementa mediante la interfaz con sufijo {@code Service} (ej. {@code SolicitudPensionService})
 * y su implementación con sufijo {@code ServiceImpl} (ej. {@code SolicitudPensionServiceImpl}),
 * o directamente como clase {@code Service} en estilos modulares.
 * <p>
 * Demuestra la separación de responsabilidades:
 * <ul>
 *   <li>Orquestación transaccional mediante {@link Transactional}.</li>
 *   <li>Inversión de dependencias mediante inyección por constructor (PR05 / DIP).</li>
 *   <li>Integración con observabilidad institucional OTEL mediante {@link NewSpan} y {@link SpanTag} (LIN-OBS-001).</li>
 *   <li>Delegación de la lógica pura previsional al servicio de dominio (PD05).</li>
 *   <li>Publicación de eventos de dominio para desacoplamiento asíncrono (LIN-BUS-001).</li>
 * </ul>
 *
 * @author OTI — Oficina de Tecnologías de la Información
 * @version 1.0
 */
@Service
public class SolicitudPensionServiceImpl implements SolicitudPensionService {

    private final AportanteRepository aportanteRepository;
    private final CalculoPensionVitaliciaDomainService calculoDomainService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Constructor para inyección de dependencias por constructor (Mandatorio según LIN-DEV-JAVA-001).
     */
    public SolicitudPensionServiceImpl(AportanteRepository aportanteRepository,
                                     CalculoPensionVitaliciaDomainService calculoDomainService,
                                     ApplicationEventPublisher eventPublisher) {
        this.aportanteRepository = aportanteRepository;
        this.calculoDomainService = calculoDomainService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Orquesta el registro transaccional de una solicitud de pensión vitalicia.
     *
     * @param dni        Documento Nacional de Identidad del aportante.
     * @param añosAporte Cantidad total de años de aporte reconocidos.
     * @return DTO de respuesta con el resultado del cálculo y estado.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @NewSpan("solicitud-pension-registrar")
    public SolicitudPensionResponse registrarSolicitud(@SpanTag("dni") String dni, int añosAporte) {
        // 1. Carga de entidad vía puerto del repositorio en la capa de dominio
        Aportante aportante = aportanteRepository.obtenerPorDni(dni)
                .orElseThrow(() -> new AportanteNoEncontradoException(dni));

        // 2. Delegación de lógica previsional al servicio de dominio puro (POJO sin Spring)
        BigDecimal montoCalculado = calculoDomainService.calcularMontoVitalicio(aportante, añosAporte);
        aportante.asignarPensionVitalicia(montoCalculado);

        // 3. Persistencia de cambios a través del repositorio
        aportanteRepository.guardar(aportante);

        // 4. Publicación de evento de dominio / integración (CloudEvents / Kafka según LIN-BUS-001)
        eventPublisher.publishEvent(new SolicitudPensionRegistradaEvent(aportante.getId(), montoCalculado));

        return new SolicitudPensionResponse(aportante.getId(), montoCalculado, "REGISTRADA");
    }
}
