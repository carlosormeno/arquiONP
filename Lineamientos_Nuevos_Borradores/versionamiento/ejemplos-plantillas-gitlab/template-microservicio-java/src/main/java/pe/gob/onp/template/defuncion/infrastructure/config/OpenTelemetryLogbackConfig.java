package pe.gob.onp.template.defuncion.infrastructure.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

/**
 * Copia idéntica del homónimo en `template-backend-java-modular` y `template-worker-java`:
 * instala el appender OTEL en logback para correlacionar logs con trazas (LIN-OBS-001).
 */
@Configuration
public class OpenTelemetryLogbackConfig {

    private final OpenTelemetry openTelemetry;

    public OpenTelemetryLogbackConfig(OpenTelemetry openTelemetry) {
        this.openTelemetry = openTelemetry;
    }

    @PostConstruct
    public void installAppender() {
        OpenTelemetryAppender.install(openTelemetry);
    }
}
