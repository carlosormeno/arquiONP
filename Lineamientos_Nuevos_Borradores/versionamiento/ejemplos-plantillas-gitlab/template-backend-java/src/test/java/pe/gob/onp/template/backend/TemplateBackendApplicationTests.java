package pe.gob.onp.template.backend;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class TemplateBackendApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnWrappedHealthcheckResponse() throws Exception {
        mockMvc.perform(get("/api/v1/template/healthcheck"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Request-ID"))
                .andExpect(jsonPath("$.codHttp").value(200))
                .andExpect(jsonPath("$.codDetRespuesta").value("000"))
                .andExpect(jsonPath("$.data").value("ok"));
    }
}
