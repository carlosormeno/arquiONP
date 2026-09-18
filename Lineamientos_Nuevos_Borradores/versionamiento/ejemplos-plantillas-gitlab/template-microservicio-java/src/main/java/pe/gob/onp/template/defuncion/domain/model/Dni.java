package pe.gob.onp.template.defuncion.domain.model;

/**
 * Value Object — identificador de persona natural peruana. Cero dependencias de framework:
 * solo Java puro (record autovalidado, Java 21).
 *
 * <p>Este microservicio independiente (Estadio 3, `ARQ-R-001`) NO importa {@code onp-common-domain}
 * de {@code template-backend-java-modular}: al ser un sistema propio y desplegable por separado,
 * no comparte Shared Kernel en memoria con ningún monolito modular (LIN-DIS-001 §3.4 — el Shared
 * Kernel es un mecanismo de módulos Maven dentro de un mismo reactor, no aplica entre sistemas
 * independientes). Si dos microservicios ONP necesitan compartir un Value Object idéntico, la vía
 * institucional es publicarlo como librería versionada propia, nunca acoplar sus builds.</p>
 */
public record Dni(String valor) {

    private static final int LONGITUD = 8;

    public Dni {
        if (valor == null || !valor.matches("\\d{" + LONGITUD + "}")) {
            throw new IllegalArgumentException(
                    "DNI inválido: debe contener exactamente " + LONGITUD + " dígitos numéricos");
        }
    }
}
