package pe.gob.onp.template.defuncion.infrastructure.client;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import pe.gob.onp.template.defuncion.domain.exception.ServicioRegistroCivilNoDisponibleException;
import pe.gob.onp.template.defuncion.domain.model.Dni;
import pe.gob.onp.template.defuncion.domain.model.EstadoDefuncion;
import pe.gob.onp.template.defuncion.domain.port.out.RegistroCivilDefuncionClient;
import pe.gob.onp.template.defuncion.infrastructure.client.dto.RegistroCivilRespuestaDto;

/**
 * Adapter de salida hacia el servicio externo de Registro Civil (RENIEC) — implementa el puerto
 * {@link RegistroCivilDefuncionClient} del dominio. Este es el punto de salida que, por tratarse
 * de un Microservicio (Estadio 3), está OBLIGADO a usar Resilience4j para Circuit Breaker y
 * Bulkhead (LIN-DIS-001 §6.2/§6.3, `DIS-R-009`) — a diferencia de `template-backend-java-modular`,
 * donde esta misma protección sería excepcional y requeriría un ADR aprobado por Arquitectura.
 *
 * <p><b>Cómo se reparten las tres protecciones de LIN-DIS-001 §6 entre HttpClient 5 y Resilience4j
 * en esta plantilla</b> (decisión de diseño, ver README "Decisiones de diseño resueltas"):</p>
 * <ul>
 *   <li><b>Timeout (§6.1, siempre obligatorio):</b> lo resuelve {@code RegistroCivilClientConfig}
 *   con Apache HttpClient 5 ({@code connectTimeout}/{@code responseTimeout}) — independiente de
 *   Resilience4j, que no es quien controla cuánto tarda una llamada síncrona ya en curso.</li>
 *   <li><b>Bulkhead (§6.3, obligatorio en Microservicio):</b> doble capa a propósito — el pool de
 *   conexiones de HttpClient 5 (`RegistroCivilClientConfig`) acota cuántas conexiones TCP pueden
 *   abrirse hacia el proveedor; el {@code @Bulkhead} de Resilience4j (semáforo, abajo) además
 *   rechaza en el propio hilo de la JVM sin siquiera intentar tomar una conexión del pool una vez
 *   se alcanza el límite — un fail-fast más barato que esperar a que el pool se agote.</li>
 *   <li><b>Circuit Breaker (§6.2, obligatorio en Microservicio):</b> exclusivamente
 *   {@code @CircuitBreaker} de Resilience4j — no existe equivalente en HttpClient 5.</li>
 * </ul>
 *
 * <p>No se usa {@code @TimeLimiter} de Resilience4j: esta llamada es síncrona (bloqueante, vía
 * {@link RestClient}), y TimeLimiter de Resilience4j exige que el método decorado devuelva
 * {@code CompletableFuture} — envolver una llamada síncrona solo para obtener un timeout que
 * HttpClient 5 ya garantiza (`responseTimeout`) sería redundante y añadiría un hilo extra sin
 * beneficio real.</p>
 */
@Component
public class RegistroCivilHttpAdapter implements RegistroCivilDefuncionClient {

    private static final Logger LOG = LoggerFactory.getLogger(RegistroCivilHttpAdapter.class);

    /** Nombre de instancia Resilience4j — debe coincidir con application.yml (resilience4j.*.instances.*). */
    static final String INSTANCIA_RESILIENCE4J = "registroCivil";

    private final RestClient registroCivilRestClient;

    public RegistroCivilHttpAdapter(RestClient registroCivilRestClient) {
        this.registroCivilRestClient = registroCivilRestClient;
    }

    @Override
    @CircuitBreaker(name = INSTANCIA_RESILIENCE4J, fallbackMethod = "consultarFallback")
    @Bulkhead(name = INSTANCIA_RESILIENCE4J)
    public EstadoDefuncion consultar(Dni dni) {
        RegistroCivilRespuestaDto respuesta = registroCivilRestClient.get()
                .uri("/v1/defunciones/{dni}", dni.valor())
                .retrieve()
                .body(RegistroCivilRespuestaDto.class);
        return mapEstado(respuesta);
    }

    /**
     * Fallback obligatorio de Resilience4j (LIN-DIS-001 §6.2) — se invoca tanto cuando el circuito
     * está ABIERTO (rechazo local, sin llamada de red) como cuando la llamada real falla. Firma
     * exigida por Resilience4j: mismos parámetros del método decorado + {@link Throwable} final.
     * Nunca retorna un valor "amigable" en silencio: traduce el fallo técnico a una excepción de
     * dominio explícita, para que el caso de uso y el cliente HTTP sepan que la verificación no
     * pudo completarse (no confundir con "la persona no fue encontrada", que es un resultado
     * válido del propio Registro Civil, no un fallo del canal).
     */
    private EstadoDefuncion consultarFallback(Dni dni, Throwable ex) {
        LOG.warn("Fallo o circuito abierto al consultar Registro Civil para dni={}: {}", dni.valor(), ex.toString());
        throw new ServicioRegistroCivilNoDisponibleException(dni.valor(), ex);
    }

    private EstadoDefuncion mapEstado(RegistroCivilRespuestaDto respuesta) {
        if (respuesta == null || respuesta.estadoCivilVital() == null) {
            return EstadoDefuncion.NO_DETERMINADO;
        }
        return switch (respuesta.estadoCivilVital()) {
            case "V" -> EstadoDefuncion.VIVO;
            case "F" -> EstadoDefuncion.FALLECIDO;
            default -> EstadoDefuncion.NO_DETERMINADO;
        };
    }
}
