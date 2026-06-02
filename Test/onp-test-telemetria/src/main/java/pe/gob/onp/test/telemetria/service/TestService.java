package pe.gob.onp.test.telemetria.service;

import io.micrometer.tracing.annotation.NewSpan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TestService {

    private static final Logger log = LoggerFactory.getLogger(TestService.class);

    public String ping() {
        return "activo";
    }

    @NewSpan("onp.test.operacion-lenta")
    public long operacionLenta(int segundos) throws InterruptedException {
        log.info("Iniciando operacion lenta. Duracion configurada: {}s", segundos);
        long inicio = System.currentTimeMillis();
        Thread.sleep(segundos * 1000L);
        long duracion = System.currentTimeMillis() - inicio;
        log.info("Operacion lenta completada. Duracion real: {}ms", duracion);
        return duracion;
    }

    public void operacionConError() {
        RuntimeException e = new RuntimeException("Error de prueba para verificar trazas de error en Jaeger");
        log.error("Operacion fallida intencionalmente para prueba de trazas", e);
        throw e;
    }

    public String eco(String mensaje) {
        log.debug("Eco recibido: {}", mensaje);
        return mensaje;
    }
}
