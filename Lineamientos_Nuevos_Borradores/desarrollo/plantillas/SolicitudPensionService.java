package pe.gob.onp.pensiones.solicitud.application.service;

import pe.gob.onp.pensiones.solicitud.domain.model.SolicitudPensionResponse;

/**
 * Interfaz de servicio de aplicación (Application Service) según nomenclatura ONP (Sección 4.2).
 * <p>
 * **Conexión con Nomenclatura Institucional (Sección 4.2 y Anexo C):**
 * En la convención de nombres de la ONP, el rol arquitectónico de "Application Service"
 * corresponde directamente a la interfaz con sufijo {@code Service} (ej. {@code SolicitudPensionService})
 * y su implementación con sufijo {@code ServiceImpl} (ej. {@code SolicitudPensionServiceImpl}).
 * <p>
 * Define el contrato transaccional y de coordinación para los casos de uso previsionales,
 * desacoplando a los controladores REST o consumidores web de la implementación de infraestructura (PR05 / DIP).
 *
 * @author OTI — Oficina de Tecnologías de la Información
 * @version 1.0
 */
public interface SolicitudPensionService {
    /**
     * Orquesta el registro transaccional de una solicitud de pensión vitalicia.
     *
     * @param dni        Documento Nacional de Identidad del aportante.
     * @param añosAporte Cantidad total de años de aporte reconocidos.
     * @return DTO de respuesta con el resultado del cálculo y estado.
     */
    SolicitudPensionResponse registrarSolicitud(String dni, int añosAporte);
}
