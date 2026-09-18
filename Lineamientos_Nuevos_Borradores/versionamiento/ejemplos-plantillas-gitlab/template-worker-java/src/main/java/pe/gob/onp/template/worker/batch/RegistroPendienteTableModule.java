package pe.gob.onp.template.worker.batch;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Table Module (LIN-DIS-001 §4.1): "un único objeto o clase de servicio organiza y calcula
 * reglas masivas sobre TODA una colección o tabla de registros en memoria al mismo tiempo, en
 * lugar de instanciar miles de objetos individuales". Reservado a procesos batch, como aquí.
 *
 * <p>Deliberadamente NO tiene dependencias de Spring/JDBC/persistencia: recibe la colección
 * completa ya cargada, la recorre una sola vez en memoria y devuelve el resultado. Eso permite
 * probarla con pruebas unitarias puras — sin Spring, sin mocks (LIN-TEST-001 §4.6, fila "Table
 * Module") — y deja el acceso a datos (lectura y {@code batchUpdate}) en
 * {@link ActualizacionMasivaJobRunner}, que sí depende de {@code JdbcTemplate}.
 *
 * <p>Regla de negocio de ejemplo: todo registro {@code PENDIENTE} con más de
 * {@link #DIAS_EXPIRACION} días de antigüedad pasa a {@code EXPIRADO}. Reemplazar por la regla
 * real del sistema — el punto de la clase es la forma (colección en memoria, un solo recorrido,
 * sin persistir fila por fila), no esta regla en particular.
 */
@Component
public class RegistroPendienteTableModule {

    public static final int DIAS_EXPIRACION = 30;

    /**
     * Aplica la regla de expiración sobre toda la colección en memoria y devuelve únicamente las
     * filas que cambiaron de estado (las que no cambiaron no se re-escriben en BD).
     */
    public List<RegistroPendienteRow> aplicarReglaExpiracion(List<RegistroPendienteRow> registros, Instant ahora) {
        return registros.stream()
                .filter(fila -> RegistroPendienteRow.ESTADO_PENDIENTE.equals(fila.estado()))
                .filter(fila -> haExpirado(fila, ahora))
                .map(fila -> fila.conEstado(RegistroPendienteRow.ESTADO_EXPIRADO))
                .toList();
    }

    private boolean haExpirado(RegistroPendienteRow fila, Instant ahora) {
        Duration antiguedad = Duration.between(fila.fechaCreacion(), ahora);
        return antiguedad.toDays() >= DIAS_EXPIRACION;
    }
}
