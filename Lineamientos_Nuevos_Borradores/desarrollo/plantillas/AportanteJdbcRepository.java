package pe.gob.onp.pensiones.solicitud.infrastructure.persistence;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import pe.gob.onp.pensiones.solicitud.domain.exception.InfrastructureException;
import pe.gob.onp.pensiones.solicitud.domain.exception.ReglaPrevisionalOracleException;
import pe.gob.onp.pensiones.solicitud.domain.model.Aportante;
import pe.gob.onp.pensiones.solicitud.domain.repository.AportanteRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Plantilla oficial de referencia para el patrón táctico PD04 (Repository - Adaptador de Infraestructura).
 * <p>
 * **Conexión con Nomenclatura Institucional (Sección 4.2, 12 y Anexo C):**
 * En la convención de nombres de la ONP, el adaptador en infraestructura adopta el sufijo
 * {@code JpaRepository} (si utiliza Spring Data JPA) o {@code JdbcRepository} / {@code OracleRepository}
 * (si utiliza {@code JdbcTemplate} o procedimientos PL/SQL en Oracle, como en este caso: {@code AportanteJdbcRepository}).
 * <p>
 * Implementa el puerto de dominio {@link AportanteRepository} cumpliendo estrictamente con:
 * <ul>
 *   <li><b>LIN-BD-ORA-001:</b> Encapsulamiento de acceso a tablas permanentes y procedimientos PL/SQL en Oracle.</li>
 *   <li><b>Uso obligatorio de Bind Variables:</b> Prevención de inyección SQL y optimización del Plan Cache de Oracle.</li>
 *   <li><b>Traducción de Excepciones:</b> Captura de errores técnicos de JDBC/JPA y mapeo a excepciones limpias
 *       de la jerarquía de aplicación, evaluando códigos de error PL/SQL (RAISE_APPLICATION_ERROR en rango -20999 a -20000).</li>
 * </ul>
 *
 * @author OTI — Oficina de Tecnologías de la Información
 * @version 1.0
 */
@Repository
public class AportanteJdbcRepository implements AportanteRepository {

    private static final String SQL_SELECT_POR_DNI =
            "SELECT ID_APORTANTE, C_DNI, N_EDAD, N_FONDO_ACUMULADO, IN_ACTIVO " +
            "FROM PE_ESQ_PENSIONES.TBL_APORTANTE " +
            "WHERE C_DNI = ? AND IN_ACTIVO = 1";

    private static final String SQL_UPDATE_PENSION =
            "UPDATE PE_ESQ_PENSIONES.TBL_APORTANTE " +
            "SET N_PENSION_ASIGNADA = ?, " +
            "    ID_USUA_MODI = COALESCE(SYS_CONTEXT('USERENV', 'CLIENT_IDENTIFIER'), USER), " +
            "    FE_USUA_MODI = SYSTIMESTAMP, " +
            "    DE_TERM_MODI = SYS_CONTEXT('USERENV', 'IP_ADDRESS') " +
            "WHERE ID_APORTANTE = ?";

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Aportante> rowMapper;

    public AportanteJdbcRepository(JdbcTemplate jdbcTemplate, RowMapper<Aportante> rowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.rowMapper = rowMapper;
    }

    @Override
    public Optional<Aportante> obtenerPorDni(String dni) {
        try {
            // Uso de bind variable (?) para consulta optimizada y segura en Oracle
            List<Aportante> resultados = jdbcTemplate.query(SQL_SELECT_POR_DNI, rowMapper, dni);
            return resultados.stream().findFirst();
        } catch (DataAccessException ex) {
            throw traducirExcepcionOracle("obtenerPorDni", ex);
        }
    }

    @Override
    public void guardar(Aportante aportante) {
        try {
            // Actualización cumpliendo campos de auditoría de LIN-BD-ORA-001 Anexo C
            jdbcTemplate.update(SQL_UPDATE_PENSION, aportante.getPensionAsignada(), aportante.getId());
        } catch (DataAccessException ex) {
            throw traducirExcepcionOracle("guardar", ex);
        }
    }

    /**
     * Traduce las excepciones técnicas del controlador JDBC/Oracle a excepciones limpias de la aplicación.
     * Preserva el SQLCODE y el mensaje de negocio sin exponer el stacktrace SQL a la capa REST.
     */
    private RuntimeException traducirExcepcionOracle(String operacion, DataAccessException ex) {
        if (ex.getRootCause() instanceof SQLException sqlEx) {
            int sqlCode = sqlEx.getErrorCode();
            // Evaluación del rango de excepciones de usuario lanzadas por PL/SQL (RAISE_APPLICATION_ERROR)
            if (sqlCode >= 20000 && sqlCode <= 20999) {
                return new ReglaPrevisionalOracleException(sqlEx.getMessage(), sqlCode);
            }
            return new InfrastructureException(
                "Error técnico de Base de Datos en " + operacion + " [SQLCODE=" + sqlCode + "]", sqlEx
            );
        }
        return new InfrastructureException("Error de acceso a datos en la operación " + operacion, ex);
    }
}
