package pe.gob.onp.template.defuncion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada único de la JVM — a diferencia de `template-backend-java-modular` (donde
 * {@code @SpringBootApplication} vive en el módulo {@code -boot} separado del resto del
 * reactor), en un Microservicio de un solo módulo Maven (LIN-DEV-JAVA-001 §14.1/§14.3) esta
 * clase convive con todo el código en el mismo {@code onp-{modulo}}.
 */
@SpringBootApplication
public class TemplateMicroservicioApplication {

    public static void main(String[] args) {
        SpringApplication.run(TemplateMicroservicioApplication.class, args);
    }
}
