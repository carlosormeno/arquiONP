package pe.gob.onp.template.backend.dto.common;

import java.time.Instant;
import java.util.List;

public class ApiResponseWrapper<T> {

    private Integer codHttp;
    private String codDetRespuesta;
    private String menDetRespuesta;
    private T data;
    private List<FieldError> errors;
    private Meta meta;

    public ApiResponseWrapper(
            Integer codHttp,
            String codDetRespuesta,
            String menDetRespuesta,
            T data,
            List<FieldError> errors,
            String requestId,
            String version) {
        this.codHttp = codHttp;
        this.codDetRespuesta = codDetRespuesta;
        this.menDetRespuesta = menDetRespuesta;
        this.data = data;
        this.errors = errors;
        this.meta = new Meta(Instant.now().toString(), requestId, version);
    }

    public static <T> ApiResponseWrapper<T> ok(T data, String requestId, String version) {
        return new ApiResponseWrapper<>(200, "000", "Operacion completada correctamente.", data, null, requestId, version);
    }

    public static <T> ApiResponseWrapper<T> error(
            int codHttp,
            String codDetRespuesta,
            String message,
            List<FieldError> errors,
            String requestId,
            String version) {
        return new ApiResponseWrapper<>(codHttp, codDetRespuesta, message, null, errors, requestId, version);
    }

    public Integer getCodHttp() {
        return codHttp;
    }

    public String getCodDetRespuesta() {
        return codDetRespuesta;
    }

    public String getMenDetRespuesta() {
        return menDetRespuesta;
    }

    public T getData() {
        return data;
    }

    public List<FieldError> getErrors() {
        return errors;
    }

    public Meta getMeta() {
        return meta;
    }

    public static class Meta {
        private String timestamp;
        private String requestId;
        private String version;

        public Meta(String timestamp, String requestId, String version) {
            this.timestamp = timestamp;
            this.requestId = requestId;
            this.version = version;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public String getRequestId() {
            return requestId;
        }

        public String getVersion() {
            return version;
        }
    }

    public static class FieldError {
        private String campo;
        private String mensaje;

        public FieldError(String campo, String mensaje) {
            this.campo = campo;
            this.mensaje = mensaje;
        }

        public String getCampo() {
            return campo;
        }

        public String getMensaje() {
            return mensaje;
        }
    }
}
