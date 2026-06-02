package pe.gob.onp.template.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.gob.onp.template.backend.dto.common.ApiResponseWrapper;

@RestController
@RequestMapping("/api/v1/template")
@Tag(name = "Template", description = "Endpoints base de la plantilla institucional")
public class TemplateController {

    private final String appVersion;

    public TemplateController(@Value("${info.app.version:0.1.0-SNAPSHOT}") String appVersion) {
        this.appVersion = appVersion;
    }

    @GetMapping("/healthcheck")
    @Operation(summary = "Endpoint base para validar la plantilla")
    public ResponseEntity<ApiResponseWrapper<String>> healthcheck() {
        String requestId = MDC.get("http.request.id");
        return ResponseEntity.ok(ApiResponseWrapper.ok("ok", requestId, appVersion));
    }
}
