package pe.gob.onp.test.telemetria.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(description = "Estructura de respuesta estandar ONP")
public class ApiResponseWrapper {

    private Integer codHttp;
    private String codDetRespuesta;
    private String menDetRespuesta;
    private Object data;
    private List<FieldError> errors;
    private Meta meta;

    public ApiResponseWrapper(Integer codHttp, String codDetRespuesta, String menDetRespuesta,
                              Object data, List<FieldError> errors,
                              String requestId, String version) {
        this.codHttp = codHttp;
        this.codDetRespuesta = codDetRespuesta;
        this.menDetRespuesta = menDetRespuesta;
        this.data = data;
        this.errors = errors;
        this.meta = new Meta(Instant.now().toString(), requestId, version);
    }

    public static ApiResponseWrapper ok(Object data, String requestId, String version) {
        return new ApiResponseWrapper(200, "000",
                "Operacion completada correctamente.", data, null, requestId, version);
    }

    public static ApiResponseWrapper error(int codHttp, String codDet,
            String msg, List<FieldError> errors, String requestId, String version) {
        return new ApiResponseWrapper(codHttp, codDet, msg, null, errors, requestId, version);
    }

    public Integer getCodHttp() { return codHttp; }
    public String getCodDetRespuesta() { return codDetRespuesta; }
    public String getMenDetRespuesta() { return menDetRespuesta; }
    public Object getData() { return data; }
    public List<FieldError> getErrors() { return errors; }
    public Meta getMeta() { return meta; }

    @Schema(description = "Metadatos de la respuesta")
    public static class Meta {
        private String timestamp;
        private String requestId;
        private String version;

        public Meta(String timestamp, String requestId, String version) {
            this.timestamp = timestamp;
            this.requestId = requestId;
            this.version = version;
        }

        public String getTimestamp() { return timestamp; }
        public String getRequestId() { return requestId; }
        public String getVersion() { return version; }
    }

    @Schema(description = "Error de validacion de un campo")
    public static class FieldError {
        private String field;
        private String message;

        public FieldError(String field, String message) {
            this.field = field;
            this.message = message;
        }

        public String getField() { return field; }
        public String getMessage() { return message; }
    }
}
