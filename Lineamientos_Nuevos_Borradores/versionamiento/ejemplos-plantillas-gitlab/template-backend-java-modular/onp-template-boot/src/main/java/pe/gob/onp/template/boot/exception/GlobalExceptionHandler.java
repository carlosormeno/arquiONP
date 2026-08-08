package pe.gob.onp.template.boot.exception;

import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pe.gob.onp.common.domain.exception.OnpDomainException;
import pe.gob.onp.common.domain.exception.RecursoNoEncontradoException;
import pe.gob.onp.common.web.ApiResponseWrapper;

/**
 * Único manejador de excepciones del sistema — vive en -boot porque debe capturar
 * las excepciones de dominio de TODOS los componentes ensamblados, no solo de uno
 * (ninguna capa -api individual declara su propio {@code @RestControllerAdvice}).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final String appVersion;

    public GlobalExceptionHandler(@Value("${info.app.version:0.1.0-SNAPSHOT}") String appVersion) {
        this.appVersion = appVersion;
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ApiResponseWrapper<Void>> handleNoEncontrado(RecursoNoEncontradoException exception) {
        return build(HttpStatus.NOT_FOUND, "404", exception.getMessage(), null);
    }

    @ExceptionHandler(OnpDomainException.class)
    public ResponseEntity<ApiResponseWrapper<Void>> handleDominio(OnpDomainException exception) {
        return build(HttpStatus.CONFLICT, "409", exception.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseWrapper<Void>> handleValidacion(MethodArgumentNotValidException exception) {
        List<ApiResponseWrapper.CampoError> errores = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new ApiResponseWrapper.CampoError(error.getField(), error.getDefaultMessage()))
                .collect(Collectors.toList());
        return build(HttpStatus.BAD_REQUEST, "400", "Error de validación en la solicitud.", errores);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseWrapper<Void>> handleGeneric(Exception exception) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "500",
                "Error interno del servidor. Referencie el requestId al equipo de soporte.", null);
    }

    private ResponseEntity<ApiResponseWrapper<Void>> build(
            HttpStatus status, String codDetRespuesta, String mensaje, List<ApiResponseWrapper.CampoError> errores) {
        String requestId = MDC.get("http.request.id");
        ApiResponseWrapper<Void> body =
                ApiResponseWrapper.error(status.value(), codDetRespuesta, mensaje, errores, requestId, appVersion);
        return ResponseEntity.status(status).body(body);
    }
}
