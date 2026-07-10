package pe.gob.onp.template.afiliacion.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pe.gob.onp.common.domain.model.Dni;
import pe.gob.onp.template.afiliacion.domain.model.Afiliado;
import pe.gob.onp.template.afiliacion.domain.port.in.RegistrarAfiliacionUseCase;

/** Slice test — solo la capa web, con el puerto de entrada mockeado (LIN-TEST-001 §4.1). */
@WebMvcTest(AfiliacionController.class)
class AfiliacionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RegistrarAfiliacionUseCase registrarAfiliacionUseCase;

    @Test
    void registrarConDatosValidosDevuelve201() throws Exception {
        when(registrarAfiliacionUseCase.registrar(any(Dni.class), anyString()))
                .thenReturn(Afiliado.registrar(new Dni("12345678"), "Juan Perez"));

        mockMvc.perform(post("/api/v1/afiliaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"dni":"12345678","nombreCompleto":"Juan Perez"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.dni").value("12345678"))
                .andExpect(jsonPath("$.data.estado").value("REGISTRADO"));
    }

    @Test
    void registrarConDniInvalidoDevuelve400() throws Exception {
        mockMvc.perform(post("/api/v1/afiliaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"dni":"123","nombreCompleto":"Juan Perez"}"""))
                .andExpect(status().isBadRequest());
    }
}
