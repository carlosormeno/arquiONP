package pe.gob.onp.template.defuncion.infrastructure.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import pe.gob.onp.template.defuncion.domain.model.ConsultaDefuncion;
import pe.gob.onp.template.defuncion.domain.model.Dni;
import pe.gob.onp.template.defuncion.domain.model.EstadoDefuncion;
import pe.gob.onp.template.defuncion.domain.port.in.ConsultarEstadoDefuncionUseCase;

/**
 * Prueba de adapter de entrada — {@code @WebMvcTest}, solo carga la capa web, sin datasource ni
 * Oracle (LIN-TEST-001). El caso de uso se mockea: el controlador no sabe (ni le importa) si el
 * bean real detrás del puerto persiste en Oracle o cachea en memoria.
 */
@WebMvcTest(ConsultaDefuncionController.class)
class ConsultaDefuncionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConsultarEstadoDefuncionUseCase consultarEstadoDefuncionUseCase;

    @Test
    void consultarDevuelveDatosDelAfiliadoConEstructuraDeRespuestaEstandar() throws Exception {
        Dni dni = new Dni("12345678");
        ConsultaDefuncion consulta = ConsultaDefuncion.registrar(dni, EstadoDefuncion.VIVO);
        when(consultarEstadoDefuncionUseCase.consultar(any(Dni.class))).thenReturn(consulta);

        mockMvc.perform(get("/api/v1/defunciones/{dni}", "12345678"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codHttp").value(200))
                .andExpect(jsonPath("$.data.dni").value("12345678"))
                .andExpect(jsonPath("$.data.estado").value("VIVO"));
    }

    @Test
    void consultarConDniInvalidoDevuelveBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/defunciones/{dni}", "123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codDetRespuesta").value("400"));
    }
}
