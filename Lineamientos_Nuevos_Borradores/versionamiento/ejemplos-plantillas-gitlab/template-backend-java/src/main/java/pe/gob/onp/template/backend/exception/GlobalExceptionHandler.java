package pe.gob.onp.template.backend.exception;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pe.gob.onp.template.backend.dto.common.ApiResponseWrapper;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final String appVersion;

    public GlobalExceptionHandler(@Value("${info.app.version:0.1.0-SNAPSHOT}") String appVersion) {
        this.appVersion = appVersion;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseWrapper<Void>> handleGeneric(Exception exception) {
        String requestId = MDC.get("http.request.id");
        ApiResponseWrapper<Void> body = ApiResponseWrapper.error(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "500",
                "Error interno del servidor. Referencie el requestId al equipo de soporte.",
                null,
                requestId,
                appVersion);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
