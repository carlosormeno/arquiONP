package pe.gob.onp.template.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Único {@code @SpringBootApplication} del sistema. Al vivir en el módulo -boot,
 * su component-scan (paquete base {@code pe.gob.onp.template}) alcanza automáticamente
 * los {@code @RestController}, {@code @Repository} y {@code @Configuration} de todos
 * los componentes de negocio ensamblados como dependencia Maven.
 */
@SpringBootApplication(scanBasePackages = "pe.gob.onp.template")
public class TemplateBackendModularApplication {

    public static void main(String[] args) {
        SpringApplication.run(TemplateBackendModularApplication.class, args);
    }
}
