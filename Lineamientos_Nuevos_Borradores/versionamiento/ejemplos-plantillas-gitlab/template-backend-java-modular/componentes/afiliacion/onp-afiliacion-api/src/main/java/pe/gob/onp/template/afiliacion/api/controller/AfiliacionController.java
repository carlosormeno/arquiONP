package pe.gob.onp.template.afiliacion.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pe.gob.onp.common.domain.model.Dni;
import pe.gob.onp.common.web.ApiResponseWrapper;
import pe.gob.onp.template.afiliacion.api.dto.AfiliacionResponse;
import pe.gob.onp.template.afiliacion.api.dto.RegistrarAfiliacionRequest;
import pe.gob.onp.template.afiliacion.application.command.RegistrarAfiliacionCommand;
import pe.gob.onp.template.afiliacion.application.dto.AfiliacionResult;
import pe.gob.onp.template.afiliacion.domain.model.Afiliado;
import pe.gob.onp.template.afiliacion.domain.port.in.RegistrarAfiliacionUseCase;

/**
 * Único punto de entrada HTTP del componente {@code afiliacion}. Depende exclusivamente
 * del puerto de entrada del dominio ({@link RegistrarAfiliacionUseCase}) — nunca de la
 * implementación concreta del caso de uso, que Spring inyecta como bean declarado en
 * -infrastructure (LIN-DEV-JAVA-001 §14.1).
 */
@RestController
@RequestMapping("/api/v1/afiliaciones")
@Tag(name = "Afiliación", description = "Registro de afiliados")
public class AfiliacionController {

    private final RegistrarAfiliacionUseCase registrarAfiliacionUseCase;
    private final String appVersion;

    public AfiliacionController(
            RegistrarAfiliacionUseCase registrarAfiliacionUseCase,
            @Value("${info.app.version:0.1.0-SNAPSHOT}") String appVersion) {
        this.registrarAfiliacionUseCase = registrarAfiliacionUseCase;
        this.appVersion = appVersion;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra un nuevo afiliado")
    public ResponseEntity<ApiResponseWrapper<AfiliacionResponse>> registrar(
            @RequestBody @Valid RegistrarAfiliacionRequest request) {
        // 1. Frontera de validación de forma (application/command)
        RegistrarAfiliacionCommand command = new RegistrarAfiliacionCommand(request.dni(), request.nombreCompleto());

        // 2. Invocación del puerto de entrada con tipos de dominio puros
        Afiliado afiliado = registrarAfiliacionUseCase.registrar(new Dni(command.dni()), command.nombreCompleto());

        // 3. Proyección interna -> contrato HTTP externo
        AfiliacionResult result = AfiliacionResult.from(afiliado);
        AfiliacionResponse response = AfiliacionResponse.from(result);

        String requestId = MDC.get("http.request.id");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseWrapper.ok(response, requestId, appVersion));
    }
}
