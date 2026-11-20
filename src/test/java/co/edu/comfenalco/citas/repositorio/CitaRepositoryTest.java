package co.edu.comfenalco.citas.repositorio;

import co.edu.comfenalco.citas.modelo.Cita;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CitaRepositoryTest {

    private static final String PROF = "Dr. Pérez";
    private static final LocalDateTime HORA = LocalDateTime.of(2030, 1, 15, 10, 0);

    @Autowired
    CitaRepository repo;

    @Test
    void debeDetectarHorarioOcupado() {
        repo.save(new Cita(PROF, HORA, "paciente-001"));

        assertThat(repo.existsByProfesionalAndFechaHora(PROF, HORA)).isTrue();
        assertThat(repo.existsByProfesionalAndFechaHora(PROF, HORA.plusHours(1))).isFalse();
        assertThat(repo.existsByProfesionalAndFechaHora("Dra. Gómez", HORA)).isFalse();
    }

    @Test
    void debeContarSoloLasCitasDelDia() {
        repo.save(new Cita(PROF, HORA, "p-1"));
        repo.save(new Cita(PROF, HORA.plusHours(2), "p-2"));
        repo.save(new Cita(PROF, HORA.withHour(23).withMinute(59), "p-3"));
        repo.save(new Cita(PROF, HORA.plusDays(1), "p-4"));

        var dia = HORA.toLocalDate();
        long count = repo.countByProfesionalAndFechaHoraBetween(
                PROF, dia.atStartOfDay(), dia.atTime(LocalTime.MAX));

        assertThat(count).isEqualTo(3);
    }
}
