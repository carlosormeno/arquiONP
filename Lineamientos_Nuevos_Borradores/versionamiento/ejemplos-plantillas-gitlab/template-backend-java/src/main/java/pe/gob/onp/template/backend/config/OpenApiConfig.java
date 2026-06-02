package pe.gob.onp.template.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openApi(
            @Value("${spring.application.name}") String appName,
            @Value("${info.app.version:0.1.0-SNAPSHOT}") String version,
            @Value("${app.description:Plantilla institucional ONP}") String description) {
        return new OpenAPI()
                .info(new Info()
                        .title(appName)
                        .version(version)
                        .description(description)
                        .contact(new Contact()
                                .name("OTI — Arquitectura")
                                .email("arquitectura@onp.gob.pe")));
    }
}
