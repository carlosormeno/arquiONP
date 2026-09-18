package pe.gob.onp.template.defuncion.infrastructure.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import pe.gob.onp.template.defuncion.domain.exception.ServicioRegistroCivilNoDisponibleException;
import pe.gob.onp.template.defuncion.domain.model.Dni;

/**
 * Prueba de adapter de infraestructura — demuestra lo exigido por LIN-DIS-001 §6.2 para el
 * Circuit Breaker obligatorio de Microservicio (`DIS-R-009`):
 *
 * <ol>
 *   <li>Que {@link RegistroCivilHttpAdapter#consultar(Dni)} está realmente anotado con
 *   {@code @CircuitBreaker} y {@code @Bulkhead} de Resilience4j (verificación por reflexión) —
 *   no un comentario ni una intención documental.</li>
 *   <li>Que la máquina de estados del Circuit Breaker se ABRE de verdad tras superar el umbral
 *   de fallos, envolviendo la llamada real del adapter contra un servidor HTTP simulado con
 *   {@link MockRestServiceServer} (no se usó WireMock: no está disponible en el repositorio
 *   Maven local de este entorno de verificación offline — ver README, "Decisiones de diseño
 *   resueltas"). La configuración de {@link CircuitBreakerConfig} replica los mismos parámetros
 *   normativos de `application.yml` (ventana, umbral 50%, 30s abierto, 10 en semi-abierto), solo
 *   con una ventana más pequeña para que la prueba sea rápida.</li>
 *   <li>Que el <i>fallback</i> real del adapter (privado, invocado por reflexión) traduce
 *   cualquier fallo a {@link ServicioRegistroCivilNoDisponibleException} — la excepción de
 *   dominio que {@code GlobalExceptionHandler} sabe mapear a HTTP 503.</li>
 * </ol>
 *
 * <p>Nota técnica: el decorado real vía {@code @CircuitBreaker}/{@code @Bulkhead} ocurre por un
 * proxy AOP de Spring, que solo se arma con el contexto Spring Boot completo (que en este
 * proyecto exige un {@code DataSource} Oracle no disponible en verificación offline). Por eso
 * esta prueba decora el mismo método del adapter real de forma programática con la librería base
 * de Resilience4j ({@code CircuitBreaker.executeSupplier}) — se prueba la máquina de estados real
 * reaccionando a fallos reales del adapter real, sin necesitar el contexto Spring completo.</p>
 */
class RegistroCivilHttpAdapterTest {

    private MockRestServiceServer mockServer;
    private RegistroCivilHttpAdapter adapter;
    private CircuitBreaker circuitBreaker;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://registro-civil.test");
        mockServer = MockRestServiceServer.bindTo(builder).build();
        adapter = new RegistroCivilHttpAdapter(builder.build());

        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(4)
                .minimumNumberOfCalls(4)
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .permittedNumberOfCallsInHalfOpenState(2)
                .build();
        circuitBreaker = CircuitBreaker.of("registroCivil-test", config);
    }

    @Test
    void metodoConsultarEstaAnotadoConCircuitBreakerYBulkhead() throws NoSuchMethodException {
        Method metodo = RegistroCivilHttpAdapter.class.getMethod("consultar", Dni.class);

        assertThat(metodo.isAnnotationPresent(io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker.class))
                .as("consultar() debe estar protegido con @CircuitBreaker (LIN-DIS-001 §6.2, obligatorio "
                        + "en Microservicio)")
                .isTrue();
        assertThat(metodo.isAnnotationPresent(Bulkhead.class))
                .as("consultar() debe estar protegido con @Bulkhead (LIN-DIS-001 §6.3, obligatorio en Microservicio)")
                .isTrue();
    }

    @Test
    void llamadaExitosaDevuelveElEstadoMapeadoYMantieneElCircuitoCerrado() {
        Dni dni = new Dni("12345678");
        mockServer.expect(requestTo("http://registro-civil.test/v1/defunciones/12345678"))
                .andRespond(withSuccess(
                        "{\"dni\":\"12345678\",\"estadoCivilVital\":\"V\"}", MediaType.APPLICATION_JSON));

        var resultado = circuitBreaker.executeSupplier(() -> adapter.consultar(dni));

        assertThat(resultado.name()).isEqualTo("VIVO");
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }

    @Test
    void circuitoSeAbreTrasSuperarElUmbralDeFallosDeLaVentana() {
        Dni dni = new Dni("87654321");
        for (int i = 0; i < 4; i++) {
            mockServer.expect(requestTo("http://registro-civil.test/v1/defunciones/87654321"))
                    .andRespond(withServerError());
        }

        for (int i = 0; i < 4; i++) {
            assertThatThrownBy(() -> circuitBreaker.executeSupplier(() -> adapter.consultar(dni)));
        }

        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
    }

    @Test
    void conElCircuitoAbiertoSeRechazaLocalmenteSinLlamarAlServidorExterno() {
        Dni dni = new Dni("11223344");
        circuitBreaker.transitionToOpenState();

        // Ningún stub registrado en mockServer para este DNI: si el adapter intentara la llamada
        // real, MockRestServiceServer fallaría la prueba por "petición inesperada". El fallo
        // esperado aquí es CallNotPermittedException, lanzada por Resilience4j ANTES de invocar
        // al Supplier — es la prueba de que el circuito abierto protege los hilos sin red.
        assertThatThrownBy(() -> circuitBreaker.executeSupplier(() -> adapter.consultar(dni)))
                .isInstanceOf(CallNotPermittedException.class);
    }

    @Test
    void fallbackDelAdapterTraduceCualquierFalloAExcepcionDeDominio() throws NoSuchMethodException {
        // El fallback es un detalle privado del adapter (Resilience4j lo invoca por AOP en
        // producción, con la firma exacta del método decorado + Throwable). Se invoca aquí por
        // reflexión para probar SU LÓGICA REAL (no una reimplementación en el test): que
        // cualquier fallo técnico se traduce a la excepción de dominio que el
        // GlobalExceptionHandler sabe mapear a HTTP 503 — nunca se deja escapar el tipo técnico
        // (RestClientException, CallNotPermittedException, etc.) hacia el caso de uso.
        Method fallback =
                RegistroCivilHttpAdapter.class.getDeclaredMethod("consultarFallback", Dni.class, Throwable.class);
        fallback.setAccessible(true);
        Dni dni = new Dni("55667788");
        Exception fallaSimulada = CallNotPermittedException.createCallNotPermittedException(circuitBreaker);

        assertThatThrownBy(() -> invocar(fallback, adapter, dni, fallaSimulada))
                .isInstanceOf(ServicioRegistroCivilNoDisponibleException.class)
                .hasMessageContaining(dni.valor());
    }

    private void invocar(Method metodo, Object objetivo, Object... argumentos) throws Throwable {
        try {
            metodo.invoke(objetivo, argumentos);
        } catch (InvocationTargetException ex) {
            throw ex.getCause();
        }
    }
}
