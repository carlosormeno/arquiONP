package pe.gob.onp.test.telemetria.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI openAPI(
            @Value("${spring.application.name}") String appName,
            @Value("${info.app.version:1.0.0}") String version,
            @Value("${app.description:Servicio web ONP}") String desc) {
        return new OpenAPI()
            .info(new Info()
                .title(appName)
                .version(version)
                .description(desc)
                .contact(new Contact()
                    .name("OTI --- Innovacion y Desarrollo")
                    .email("oti@onp.gob.pe")))
            .externalDocs(new ExternalDocumentation()
                .description("Documentacion interna ONP")
                .url("https://gitlab.onp.gob.pe"));
    }
}
