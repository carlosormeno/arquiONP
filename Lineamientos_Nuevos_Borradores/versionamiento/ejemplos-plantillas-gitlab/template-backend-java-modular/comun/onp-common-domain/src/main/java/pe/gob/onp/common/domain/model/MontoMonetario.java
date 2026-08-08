package pe.gob.onp.common.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value Object universal — monto monetario con moneda explícita.
 * BigDecimal obligatorio para valores monetarios (LIN-DEV-JAVA-001 §6.7) — nunca double/float.
 */
public record MontoMonetario(BigDecimal valor, String moneda) {

    public MontoMonetario {
        Objects.requireNonNull(valor, "El monto no puede ser nulo");
        Objects.requireNonNull(moneda, "La moneda no puede ser nula");
        if (valor.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El monto monetario no puede ser negativo");
        }
    }

    public MontoMonetario sumar(MontoMonetario otro) {
        if (!this.moneda.equals(otro.moneda)) {
            throw new IllegalArgumentException(
                    "No se pueden sumar montos de monedas distintas: " + this.moneda + " vs " + otro.moneda);
        }
        return new MontoMonetario(this.valor.add(otro.valor), this.moneda);
    }
}
