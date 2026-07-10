package pe.gob.onp.common.domain.model;

/**
 * Value Object universal — identificador de persona natural peruana.
 * Cero dependencias de framework: solo Java puro (records autovalidados, Java 21).
 */
public record Dni(String valor) {

    private static final int LONGITUD = 8;

    public Dni {
        if (valor == null || !valor.matches("\\d{" + LONGITUD + "}")) {
            throw new IllegalArgumentException("DNI inválido: debe contener exactamente " + LONGITUD + " dígitos numéricos");
        }
    }
}
