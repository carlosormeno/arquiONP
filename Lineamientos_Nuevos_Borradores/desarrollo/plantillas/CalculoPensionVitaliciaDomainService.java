package pe.gob.onp.pensiones.solicitud.domain.service;

import pe.gob.onp.pensiones.solicitud.domain.exception.ReglaPrevisionalException;
import pe.gob.onp.pensiones.solicitud.domain.model.Aportante;
import pe.gob.onp.pensiones.solicitud.domain.repository.TablaActuarialRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Plantilla oficial de referencia para el patrón táctico PD05 (Domain Service).
 * <p>
 * **Conexión con Nomenclatura Institucional (Sección 4.2 y Anexo C):**
 * En la convención de nombres de la ONP, los servicios de dominio puros adoptan
 * obligatoriamente el sufijo {@code DomainService} (ej. {@code CalculoPensionVitaliciaDomainService}).
 * <p>
 * Demuestra la pureza hexagonal del Dominio institucional:
 * <ul>
 *   <li><b>POJO 100% Puro:</b> Queda estrictamente prohibido incluir en esta clase anotaciones
 *       de infraestructura de Spring (@Service, @Component, @Transactional, @Autowired, @Value)
 *       ni de persistencia JPA (@Entity, @Table).</li>
 *   <li>Su registro en el contenedor de Spring Boot se realiza mediante clases @Configuration / @Bean
 *       en la capa de aplicación o configuración del módulo (Sección 11.5.2 de LIN-DEV-JAVA-001).</li>
 *   <li>Encapsula reglas previsionales actuariales que involucran múltiples entidades o tablas de ley.</li>
 * </ul>
 *
 * @author OTI — Oficina de Tecnologías de la Información
 * @version 1.0
 */
public class CalculoPensionVitaliciaDomainService {

    private static final int ANOS_MINIMOS_LEY = 20;
    private static final BigDecimal TOPE_MAXIMO_PENSION = new BigDecimal("893.00");

    private final TablaActuarialRepository tablaActuarialRepository;

    /**
     * Constructor puro para inyección por parámetro desde la configuración o aplicación.
     */
    public CalculoPensionVitaliciaDomainService(TablaActuarialRepository tablaActuarialRepository) {
        this.tablaActuarialRepository = tablaActuarialRepository;
    }

    /**
     * Ejecuta el cálculo previsional de renta vitalicia según normativa vigente.
     *
     * @param aportante  Entidad de dominio del aportante.
     * @param añosAporte Cantidad de años aportados al Sistema Nacional de Pensiones.
     * @return Monto mensual calculado para la pensión vitalicia.
     * @throws ReglaPrevisionalException Si no cumple los años mínimos de aporte exigidos por ley.
     */
    public BigDecimal calcularMontoVitalicio(Aportante aportante, int añosAporte) {
        if (añosAporte < ANOS_MINIMOS_LEY) {
            throw new ReglaPrevisionalException(
                "El aportante no cumple con los " + ANOS_MINIMOS_LEY + " años de aporte mínimos exigidos por ley."
            );
        }

        // Obtención de factor actuarial desde puerto de dominio
        BigDecimal factorEsperanzaVida = tablaActuarialRepository.obtenerFactorEsperanzaVida(aportante.getEdad());
        
        // Cálculo previsional puro
        BigDecimal montoBase = aportante.getFondoAcumulado()
                .multiply(factorEsperanzaVida)
                .setScale(2, RoundingMode.HALF_UP);

        // Aplicación de invariante de tope legal
        return montoBase.compareTo(TOPE_MAXIMO_PENSION) > 0 ? TOPE_MAXIMO_PENSION : montoBase;
    }
}
