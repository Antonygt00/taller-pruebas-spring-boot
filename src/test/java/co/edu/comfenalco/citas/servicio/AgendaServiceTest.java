package co.edu.comfenalco.citas.servicio;

import co.edu.comfenalco.citas.modelo.Cita;
import co.edu.comfenalco.citas.repositorio.CitaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgendaServiceTest {

    private static final String PROF = "Dr. Pérez";
    private static final LocalDateTime HORA = LocalDateTime.of(2030, 1, 15, 10, 0);

    @Mock
    CitaRepository repo;

    @InjectMocks
    AgendaService agenda;

    @Test
    @DisplayName("CP-01: reserva un horario libre")
    void debeReservarHorarioDisponible() {
        when(repo.existsByProfesionalAndFechaHora(PROF, HORA)).thenReturn(false);
        when(repo.countByProfesionalAndFechaHoraBetween(eq(PROF), any(), any())).thenReturn(0L);
        when(repo.saveAndFlush(any(Cita.class))).thenAnswer(inv -> inv.getArgument(0));

        Cita cita = agenda.reservar(PROF, HORA, "paciente-001");

        assertThat(cita.getPacienteId()).isEqualTo("paciente-001");
        verify(repo).saveAndFlush(any(Cita.class));
    }

    @Test
    @DisplayName("CP-04: horario ocupado")
    void noDebePermitirDobleReserva() {
        when(repo.existsByProfesionalAndFechaHora(PROF, HORA)).thenReturn(true);

        assertThatThrownBy(() -> agenda.reservar(PROF, HORA, "paciente-002"))
                .isInstanceOf(HorarioNoDisponibleException.class);
        verify(repo, never()).saveAndFlush(any(Cita.class));
    }

    @ParameterizedTest
    @ValueSource(longs = {8, 10, 19})
    @DisplayName("CP-02: acepta mientras haya cupo")
    void debeAceptarMientrasHayaCupo(long previas) {
        when(repo.existsByProfesionalAndFechaHora(PROF, HORA)).thenReturn(false);
        when(repo.countByProfesionalAndFechaHoraBetween(eq(PROF), any(), any())).thenReturn(previas);
        when(repo.saveAndFlush(any(Cita.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThatCode(() -> agenda.reservar(PROF, HORA, "paciente-003")).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("CP-03: rechaza cuando el cupo está lleno")
    void debeRechazarCuandoElCupoEstaLleno() {
        when(repo.existsByProfesionalAndFechaHora(PROF, HORA)).thenReturn(false);
        when(repo.countByProfesionalAndFechaHoraBetween(eq(PROF), any(), any())).thenReturn(20L);

        assertThatThrownBy(() -> agenda.reservar(PROF, HORA, "paciente-004"))
                .isInstanceOf(CupoDiarioExcedidoException.class);
        verify(repo, never()).saveAndFlush(any(Cita.class));
    }
}
