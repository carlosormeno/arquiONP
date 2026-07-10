package pe.gob.onp.template.afiliacion.api;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Arranque mínimo de Spring SOLO para pruebas de este módulo. En un Monolito Modular
 * multi-módulo, {@code @WebMvcTest}/{@code @SpringBootTest} buscan una clase
 * {@code @SpringBootConfiguration} recorriendo hacia arriba el paquete del test — y
 * la única real ({@code TemplateBackendModularApplication}) vive en -boot, un módulo
 * que -api ni siquiera conoce (boot depende de api, no al revés). Sin esta clase, todo
 * slice test de -api falla con "Unable to find a @SpringBootConfiguration". Cada módulo
 * -api debe declarar la suya, en el paquete raíz del módulo (nunca en producción).
 */
@SpringBootApplication
class TestAfiliacionApiConfiguration {
}
