package br.com.medflow.patient.web;

import br.com.medflow.patient.application.PatientService;
import br.com.medflow.patient.application.dto.PatientDtoRequest;
import br.com.medflow.patient.application.dto.PatientDtoResponse;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

/**
 * Protege o contrato HTTP do recurso de pacientes sem inicializar banco ou infraestrutura.
 *
 * O MockMvc executa o mapeamento MVC real; somente a Service é simulada para manter
 * estes testes focados na responsabilidade do Controller.
 */
class PatientControllerTest {

    private final PatientService patientService = mock(PatientService.class);
    private final MockMvc mockMvc = standaloneSetup(new PatientController(patientService))
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();

    @Test
    void deveBuscarPacientePorId() throws Exception {
        UUID id = UUID.fromString("76cb605b-71dc-47e8-83b5-83f2ef99a7cf");
        when(patientService.findById(id)).thenReturn(response(id));

        mockMvc.perform(get("/patients/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.fullName").value("Vinícius Oliveira"));
    }

    @Test
    void deveListarPacientesPaginados() throws Exception {
        UUID id = UUID.fromString("76cb605b-71dc-47e8-83b5-83f2ef99a7cf");
        when(patientService.findAll(any())).thenReturn(new PageImpl<>(
                List.of(response(id)),
                PageRequest.of(0, 10),
                1
        ));

        mockMvc.perform(get("/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id.toString()))
                .andExpect(jsonPath("$.content[0].fullName").value("Vinícius Oliveira"));
    }

    @Test
    void deveCriarPaciente() throws Exception {
        UUID id = UUID.fromString("76cb605b-71dc-47e8-83b5-83f2ef99a7cf");
        PatientDtoRequest request = request();
        when(patientService.create(request)).thenReturn(response(id));

        mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Vinícius Oliveira",
                                  "cpf": "529.982.247-25",
                                  "birthDate": "2000-01-15"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()));

        verify(patientService).create(request);
    }

    @Test
    void deveAtualizarPaciente() throws Exception {
        UUID id = UUID.fromString("76cb605b-71dc-47e8-83b5-83f2ef99a7cf");
        PatientDtoRequest request = request();
        when(patientService.update(eq(id), eq(request))).thenReturn(response(id));

        mockMvc.perform(put("/patients/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Vinícius Oliveira",
                                  "cpf": "529.982.247-25",
                                  "birthDate": "2000-01-15"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));

        verify(patientService).update(id, request);
    }

    @Test
    void deveExcluirPaciente() throws Exception {
        UUID id = UUID.fromString("76cb605b-71dc-47e8-83b5-83f2ef99a7cf");

        mockMvc.perform(delete("/patients/{id}", id))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(patientService).delete(id);
    }

    private PatientDtoRequest request() {
        return new PatientDtoRequest(
                "Vinícius Oliveira",
                "529.982.247-25",
                LocalDate.of(2000, 1, 15)
        );
    }

    private PatientDtoResponse response(UUID id) {
        return new PatientDtoResponse(
                id,
                "Vinícius Oliveira",
                "529.982.247-25",
                LocalDate.of(2000, 1, 15),
                Instant.parse("2026-09-12T03:00:00Z")
        );
    }
}
