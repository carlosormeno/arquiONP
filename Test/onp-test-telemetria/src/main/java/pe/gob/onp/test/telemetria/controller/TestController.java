package pe.gob.onp.test.telemetria.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.gob.onp.test.telemetria.dto.common.ApiResponseWrapper;
import pe.gob.onp.test.telemetria.service.TestService;

import java.util.Map;

@Tag(name = "Test Telemetria",
     description = "Operaciones de prueba para verificar trazas, logs y documentacion Swagger.")
@RestController
@RequestMapping("/api/test")
public class TestController {

    private final TestService testService;

    @Value("${info.app.version:1.0.0}")
    private String version;

    public TestController(TestService testService) {
        this.testService = testService;
    }

    @Operation(
        summary = "Verificar estado del servicio",
        description = "Retorna el estado actual del servicio. Genera un span HTTP automatico en Jaeger.")
    @ApiResponses({
        @ApiResponse(responseCode = "200",
            description = "Servicio operativo.",
            content = @Content(schema = @Schema(implementation = ApiResponseWrapper.class))),
        @ApiResponse(responseCode = "500",
            description = "Error interno del servidor.",
            content = @Content(schema = @Schema(implementation = ApiResponseWrapper.class)))
    })
    @GetMapping
    public ResponseEntity<ApiResponseWrapper> ping() {
        Object data = Map.of(
                "servicio", "onp-test-telemetria",
                "estado", testService.ping(),
                "mensaje", "Servicio de prueba operativo");
        return ResponseEntity.ok(ApiResponseWrapper.ok(data, requestId(), version));
    }

    @Operation(
        summary = "Ejecutar operacion lenta",
        description = "Simula una operacion que tarda N segundos. Genera un span @NewSpan visible en Jaeger.")
    @ApiResponses({
        @ApiResponse(responseCode = "200",
            description = "Operacion completada. Retorna la duracion real en milisegundos.",
            content = @Content(schema = @Schema(implementation = ApiResponseWrapper.class))),
        @ApiResponse(responseCode = "500",
            description = "Error interno del servidor.",
            content = @Content(schema = @Schema(implementation = ApiResponseWrapper.class)))
    })
    @GetMapping("/lento/{segundos}")
    public ResponseEntity<ApiResponseWrapper> lento(@PathVariable int segundos)
            throws InterruptedException {
        long duracion = testService.operacionLenta(segundos);
        Object data = Map.of("operacion", "lenta", "duracionMs", duracion);
        return ResponseEntity.ok(ApiResponseWrapper.ok(data, requestId(), version));
    }

    @Operation(
        summary = "Provocar error de prueba",
        description = "Lanza una excepcion no controlada. Verifica que el span aparece en rojo en Jaeger.")
    @ApiResponses({
        @ApiResponse(responseCode = "500",
            description = "Error de prueba generado intencionalmente.",
            content = @Content(schema = @Schema(implementation = ApiResponseWrapper.class)))
    })
    @GetMapping("/error")
    public ResponseEntity<ApiResponseWrapper> error() {
        testService.operacionConError();
        return ResponseEntity.ok(ApiResponseWrapper.ok(null, requestId(), version));
    }

    @Operation(
        summary = "Retornar eco del mensaje",
        description = "Devuelve el mismo mensaje recibido. Util para verificar logs DEBUG en DEV.")
    @ApiResponses({
        @ApiResponse(responseCode = "200",
            description = "Eco del mensaje recibido.",
            content = @Content(schema = @Schema(implementation = ApiResponseWrapper.class))),
        @ApiResponse(responseCode = "500",
            description = "Error interno del servidor.",
            content = @Content(schema = @Schema(implementation = ApiResponseWrapper.class)))
    })
    @GetMapping("/eco/{mensaje}")
    public ResponseEntity<ApiResponseWrapper> eco(@PathVariable String mensaje) {
        Object data = Map.of("eco", testService.eco(mensaje));
        return ResponseEntity.ok(ApiResponseWrapper.ok(data, requestId(), version));
    }

    private String requestId() {
        return MDC.get("http.request.id");
    }
}