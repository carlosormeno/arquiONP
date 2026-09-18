package pe.gob.onp.template.defuncion.infrastructure.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.gob.onp.template.defuncion.domain.model.ConsultaDefuncion;
import pe.gob.onp.template.defuncion.domain.model.Dni;
import pe.gob.onp.template.defuncion.domain.port.in.ConsultarEstadoDefuncionUseCase;
import pe.gob.onp.template.defuncion.infrastructure.web.dto.ConsultaDefuncionResponse;

/**
 * Único punto de entrada HTTP de este microservicio. Depende exclusivamente del puerto de
 * entrada del dominio ({@link ConsultarEstadoDefuncionUseCase}) — nunca de la implementación
 * concreta del caso de uso, que Spring inyecta como bean declarado en
 * {@code infrastructure.config.UseCaseConfig} (LIN-DEV-JAVA-001 §14.1).
 */
@RestController
@RequestMapping("/api/v1/defunciones")
@Tag(name = "Verificación de Defunción", description = "Consulta de estado vital contra el Registro Civil (RENIEC)")
public class ConsultaDefuncionController {

    private final ConsultarEstadoDefuncionUseCase consultarEstadoDefuncionUseCase;
    private final String appVersion;

    public ConsultaDefuncionController(
            ConsultarEstadoDefuncionUseCase consultarEstadoDefuncionUseCase,
            @Value("${info.app.version:0.1.0-SNAPSHOT}") String appVersion) {
        this.consultarEstadoDefuncionUseCase = consultarEstadoDefuncionUseCase;
        this.appVersion = appVersion;
    }

    @GetMapping("/{dni}")
    @Operation(summary = "Verifica el estado de defunción de una persona por DNI")
    public ResponseEntity<ApiResponseWrapper<ConsultaDefuncionResponse>> consultar(@PathVariable String dni) {
        // 1. Invocación del puerto de entrada con tipos de dominio puros (el VO Dni auto-valida
        //    la forma del path variable — LIN-DIS-001 §3.2)
        ConsultaDefuncion consulta = consultarEstadoDefuncionUseCase.consultar(new Dni(dni));

        // 2. Proyección interna -> contrato HTTP externo
        ConsultaDefuncionResponse response = ConsultaDefuncionResponse.from(consulta);

        String requestId = MDC.get("http.request.id");
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseWrapper.ok(response, requestId, appVersion));
    }
}
