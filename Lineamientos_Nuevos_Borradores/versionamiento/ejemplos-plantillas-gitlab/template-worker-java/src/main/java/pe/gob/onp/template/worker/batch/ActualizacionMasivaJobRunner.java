package pe.gob.onp.template.worker.batch;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * EJEMPLO DE JOB PUNTUAL y de JOB RECURRENTE (LIN-K8S-001 §4.1) — es la MISMA clase para ambos
 * casos. Un Job puntual (migración, carga controlada) y un Job recurrente (tarea programada) son
 * el mismo tipo de aplicación Java: lo único que cambia es qué recurso K8s la invoca.
 * <ul>
 *   <li>Invocada una vez por {@code k8s/base/job.yaml} → es un Job puntual.</li>
 *   <li>Invocada periódicamente por {@code k8s/base/cronjob.yaml} (campo {@code schedule}) →
 *       es un Job recurrente.</li>
 * </ul>
 * Ver README, sección "Worker vs. Job puntual vs. Job recurrente".
 *
 * <p>Solo se activa con el perfil Spring {@code job} — los pods {@code worker} no la ejecutan.
 * Sigue el patrón Table Module de {@code LIN-DIS-001 §4.1}: carga toda la colección pendiente en
 * memoria de una sola vez, aplica la regla con {@link RegistroPendienteTableModule} (sin
 * Spring/JDBC) y escribe el resultado con un único {@code batchUpdate}, no fila por fila.
 *
 * <p><b>Sobre {@code System.exit(...)}:</b> la orquestación (leer, transformar, escribir,
 * decidir éxito/fallo) vive en {@link #ejecutar(ApplicationArguments)}, que devuelve un código de
 * salida y es 100% unit-testeable con mocks porque nunca llama a {@code System.exit}. El método
 * {@link #run(ApplicationArguments)} de {@code ApplicationRunner} es la única línea con el efecto
 * de proceso — separarlo así evita que una prueba unitaria mate la JVM del test runner.
 */
@Component
@Profile("job")
public class ActualizacionMasivaJobRunner implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(ActualizacionMasivaJobRunner.class);

    private static final String SQL_SELECT_PENDIENTES =
            "SELECT reg_id, reg_estado, reg_fecha_creacion FROM tb_registro_pendiente "
                    + "WHERE reg_estado = ? FETCH FIRST ? ROWS ONLY";

    private static final String SQL_UPDATE_ESTADO =
            "UPDATE tb_registro_pendiente SET reg_estado = ?, reg_fecha_actualizacion = ? WHERE reg_id = ?";

    private final JdbcTemplate jdbcTemplate;
    private final RegistroPendienteTableModule tableModule;
    private final int tamanioPagina;

    public ActualizacionMasivaJobRunner(
            JdbcTemplate jdbcTemplate,
            RegistroPendienteTableModule tableModule,
            @Value("${onp.worker.job.tamanio-pagina:500}") int tamanioPagina) {
        this.jdbcTemplate = jdbcTemplate;
        this.tableModule = tableModule;
        this.tamanioPagina = tamanioPagina;
    }

    @Override
    public void run(ApplicationArguments args) {
        System.exit(ejecutar(args));
    }

    /**
     * Ejecuta la actualización masiva y devuelve el código de salida del proceso: {@code 0} en
     * éxito, {@code 1} en fallo. No llama a {@code System.exit} — eso es responsabilidad
     * exclusiva de {@link #run(ApplicationArguments)}, para que este método se pueda probar sin
     * terminar la JVM (ver Javadoc de la clase).
     */
    int ejecutar(ApplicationArguments args) {
        try {
            List<RegistroPendienteRow> pendientes = leerPendientes();
            LOG.info("Registros pendientes leidos={}", pendientes.size());

            List<RegistroPendienteRow> actualizados =
                    tableModule.aplicarReglaExpiracion(pendientes, Instant.now());
            LOG.info("Registros a expirar={}", actualizados.size());

            escribirCambios(actualizados);
            LOG.info("Actualizacion masiva completada exitosamente");
            return 0;
        } catch (RuntimeException ex) {
            LOG.error("Actualizacion masiva fallida, no se aplico ningun cambio pendiente", ex);
            return 1;
        }
    }

    private List<RegistroPendienteRow> leerPendientes() {
        return jdbcTemplate.query(
                SQL_SELECT_PENDIENTES,
                (rs, rowNum) -> new RegistroPendienteRow(
                        rs.getLong("reg_id"),
                        rs.getString("reg_estado"),
                        rs.getTimestamp("reg_fecha_creacion").toInstant()),
                RegistroPendienteRow.ESTADO_PENDIENTE,
                tamanioPagina);
    }

    private void escribirCambios(List<RegistroPendienteRow> actualizados) {
        if (actualizados.isEmpty()) {
            return;
        }
        Instant ahora = Instant.now();
        // Un único batchUpdate para todo el lote — no una sentencia UPDATE por fila
        // (justamente lo que el patrón Table Module busca evitar, LIN-DIS-001 §4.1).
        jdbcTemplate.batchUpdate(SQL_UPDATE_ESTADO, actualizados, actualizados.size(),
                (ps, fila) -> {
                    ps.setString(1, fila.estado());
                    ps.setTimestamp(2, Timestamp.from(ahora));
                    ps.setLong(3, fila.id());
                });
    }
}
