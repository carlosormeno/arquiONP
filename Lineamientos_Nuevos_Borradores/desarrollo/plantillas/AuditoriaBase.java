package pe.gob.onp.pensiones.expedientes.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.slf4j.MDC;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;

@MappedSuperclass
public abstract class AuditoriaBase {

    @Column(name = "ID_USUA_CREA", length = 30, nullable = false, updatable = false)
    private String idUsuaCrea;

    @Column(name = "FE_USUA_CREA", nullable = false, updatable = false)
    private Instant feUsuaCrea;

    @Column(name = "DE_TERM_CREA", length = 39, nullable = false, updatable = false)
    private String deTermCrea;

    @Column(name = "ID_USUA_MODI", length = 30)
    private String idUsuaModi;

    @Column(name = "FE_USUA_MODI")
    private Instant feUsuaModi;

    @Column(name = "DE_TERM_MODI", length = 39)
    private String deTermModi;

    @PrePersist
    protected void onCrear() {
        String usuario  = resolverUsuario();
        String terminal = resolverTerminal();
        this.idUsuaCrea = usuario;
        this.feUsuaCrea = Instant.now();
        this.deTermCrea = terminal;
        // Los campos _MODI quedan NULL en la creación, conforme a LIN-BD-ORA-001 §5.3
        this.idUsuaModi = null;
        this.feUsuaModi = null;
        this.deTermModi = null;
    }

    @PreUpdate
    protected void onModificar() {
        // Los campos _CREA nunca se modifican (updatable = false en columna).
        this.idUsuaModi = resolverUsuario();
        this.feUsuaModi = Instant.now();
        this.deTermModi = resolverTerminal();
    }

    // Lee el usuario del MDC, donde SaaTokenValidationFilter (@Order 2) lo puso.
    // En jobs o batch sin contexto HTTP retorna "sistema".
    private String resolverUsuario() {
        String userId = MDC.get("user.id");
        return (userId != null && !userId.isBlank()) ? userId : "sistema";
    }

    // Obtiene la IP real del cliente respetando proxies y el gateway WSO2.
    // Trunca a 39 caracteres (máximo de DE_TERM_CREA / DE_TERM_MODI en Oracle).
    private String resolverTerminal() {
        try {
            ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                String ip = attrs.getRequest().getHeader("X-Forwarded-For");
                if (ip == null || ip.isBlank()) {
                    ip = attrs.getRequest().getRemoteAddr();
                } else {
                    // X-Forwarded-For puede tener múltiples IPs separadas por coma;
                    // la primera es la del cliente original.
                    ip = ip.split(",")[0].trim();
                }
                return ip.substring(0, Math.min(ip.length(), 39));
            }
        } catch (Exception ignored) {}
        return "N/A";
    }
}
