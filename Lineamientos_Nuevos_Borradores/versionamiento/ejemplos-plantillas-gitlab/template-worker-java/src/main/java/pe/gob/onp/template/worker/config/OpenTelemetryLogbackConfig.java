package pe.gob.onp.template.worker.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

/**
 * Copia idéntica del homónimo en {@code template-backend-java}: instala el appender OTEL en
 * logback para correlacionar logs con trazas (LIN-OBS-001). Funciona igual con o sin servidor
 * web embebido — el exportador OTLP usa un cliente HTTP saliente propio, independiente de
 * {@code spring.main.web-application-type} (ver application.yml).
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
