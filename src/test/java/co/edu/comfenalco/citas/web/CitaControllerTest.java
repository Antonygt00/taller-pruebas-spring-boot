package co.edu.comfenalco.citas.web;

import co.edu.comfenalco.citas.modelo.Cita;
import co.edu.comfenalco.citas.servicio.AgendaService;
import co.edu.comfenalco.citas.servicio.HorarioNoDisponibleException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CitaController.class)
class CitaControllerTest {

    private static final String JSON_VALIDO = """
            {
              "profesional": "Dr. Pérez",
              "fechaHora": "2030-11-05T08:00:00",
              "pacienteId": "paciente-001"
            }
            """;

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AgendaService agenda;

    @Test
    void debeResponder201AlReservar() throws Exception {
        when(agenda.reservar(any(), any(), any()))
                .thenReturn(new Cita("Dr. Pérez", LocalDateTime.of(2030, 11, 5, 8, 0), "paciente-001"));

        mockMvc.perform(post("/api/citas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.profesional").value("Dr. Pérez"));
    }

    @Test
    void debeResponder409SiElHorarioEstaOcupado() throws Exception {
        when(agenda.reservar(any(), any(), any()))
                .thenThrow(new HorarioNoDisponibleException("Horario ocupado"));

        mockMvc.perform(post("/api/citas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void debeResponder400SiFaltanDatos() throws Exception {
        String json = """
                {
                  "profesional": "",
                  "fechaHora": "2030-11-05T08:00:00",
                  "pacienteId": "p-1"
                }
                """;

        mockMvc.perform(post("/api/citas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(agenda);
    }

    @Test
    void debeResponder400SiLaFechaEsPasada() throws Exception {
        String json = """
                {
                  "profesional": "Dr. Pérez",
                  "fechaHora": "2020-01-01T08:00:00",
                  "pacienteId": "p-1"
                }
                """;

        mockMvc.perform(post("/api/citas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(agenda);
    }
}
