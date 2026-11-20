package co.edu.comfenalco.citas.ui;

import co.edu.comfenalco.citas.repositorio.CitaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("ui")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReservaUITest {

    @LocalServerPort
    int puerto;

    @Autowired
    CitaRepository repo;

    WebDriver driver;

    @BeforeEach
    void iniciar() {
        repo.deleteAll();
        ChromeOptions opciones = new ChromeOptions();
        opciones.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage", "--window-size=1280,800");
        driver = new ChromeDriver(opciones);
    }

    @AfterEach
    void cerrar() {
        if (driver != null) {
            driver.quit();
        }
    }

    private void completarFormulario(String profesional, String fechaHora, String paciente) {
        driver.findElement(By.id("profesional")).clear();
        driver.findElement(By.id("profesional")).sendKeys(profesional);

        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript(
                "const el = arguments[0]; el.value = arguments[1]; el.dispatchEvent(new Event('change', {bubbles:true}));",
                driver.findElement(By.id("fechaHora")),
                fechaHora
        );

        driver.findElement(By.id("pacienteId")).clear();
        driver.findElement(By.id("pacienteId")).sendKeys(paciente);
        driver.findElement(By.id("btnReservar")).click();
    }

    private void esperarMensaje(String texto) {
        boolean encontrado = new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.textToBePresentInElementLocated(By.id("mensaje"), texto));
        assertThat(encontrado).isTrue();
    }

    @Test
    void debeConfirmarLaReservaAlPaciente() {
        driver.get("http://localhost:" + puerto + "/citas.html");
        completarFormulario("Dra. Gómez", "2030-11-05T09:00", "paciente-100");
        esperarMensaje("Cita reservada");
    }

    @Test
    void debeRechazarDobleReserva() {
        driver.get("http://localhost:" + puerto + "/citas.html");
        completarFormulario("Dr. López", "2030-11-06T10:00", "paciente-101");
        esperarMensaje("Cita reservada");
        completarFormulario("Dr. López", "2030-11-06T10:00", "paciente-102");
        esperarMensaje("Horario no disponible");
    }
}
