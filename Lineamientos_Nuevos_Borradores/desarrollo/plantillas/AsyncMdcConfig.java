package pe.gob.onp.pensiones.expedientes.config;

import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.context.annotation.Bean;
import java.util.Map;

@Configuration
@EnableAsync
public class AsyncMdcConfig {

    @Bean
    public TaskDecorator mdcTaskDecorator() {
        return runnable -> {
            // Captura el contexto del hilo padre
            Map<String, String> contextMap = MDC.getCopyOfContextMap();
            return () -> {
                try {
                    if (contextMap != null) {
                        MDC.setContextMap(contextMap);
                    }
                    runnable.run();
                } finally {
                    // Limpia el MDC al retornar el hilo al pool
                    MDC.clear();
                }
            };
        };
    }
}
