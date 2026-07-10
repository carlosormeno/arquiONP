package pe.gob.onp.template.afiliacion.infrastructure;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Arranque mínimo de Spring SOLO para pruebas de este módulo — mismo motivo que
 * {@code TestAfiliacionApiConfiguration} en el módulo -api: {@code @DataJpaTest}
 * necesita una clase {@code @SpringBootConfiguration} alcanzable desde este paquete,
 * y la real vive en -boot, inaccesible desde aquí.
 */
@SpringBootApplication
class TestAfiliacionInfrastructureConfiguration {
}
