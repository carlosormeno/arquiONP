package pe.gob.onp.template.defuncion.infrastructure.web;

import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pe.gob.onp.template.defuncion.domain.exception.ConsultaDefuncionDomainException;
import pe.gob.onp.template.defuncion.domain.exception.ServicioRegistroCivilNoDisponibleException;

/**
 * Único manejador de excepciones del microservicio — al ser un solo módulo desplegable (a
 * diferencia de `template-backend-java-modular`, donde vive en el -boot ensamblador), este
 * {@code @RestControllerAdvice} vive junto al controlador, en {@code infrastructure.web}.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final String appVersion;

    public GlobalExceptionHandler(@Value("${info.app.version:0.1.0-SNAPSHOT}") String appVersion) {
        this.appVersion = appVersion;
    }

    /**
     * Mapea el fallo del punto de salida externo (Circuit Breaker abierto o llamada fallida,
     * LIN-DIS-001 §6.2) a 503 — nunca a 500: el problema es de disponibilidad de un tercero, no
     * un error interno del microservicio. Coherente con LIN-API-REST-001 §8.3.
     */
    @ExceptionHandler(ServicioRegistroCivilNoDisponibleException.class)
    public ResponseEntity<ApiResponseWrapper<Void>> handleServicioNoDisponible(
            ServicioRegistroCivilNoDisponibleException exception) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, "503", exception.getMessage(), null);
    }

    @ExceptionHandler(ConsultaDefuncionDomainException.class)
    public ResponseEntity<ApiResponseWrapper<Void>> handleDominio(ConsultaDefuncionDomainException exception) {
        return build(HttpStatus.CONFLICT, "409", exception.getMessage(), null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseWrapper<Void>> handleArgumentoInvalido(IllegalArgumentException exception) {
        return build(HttpStatus.BAD_REQUEST, "400", exception.getMessage(), null);
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
