# citas-medicas

Taller de pruebas de software en Spring Boot — reserva de citas médicas.

## Integrantes

- Esteban Pautt
- Antony Goez
- Stiven Alvarez
- Juan Cantillo

## Requisitos

- JDK 25
- Maven
- Google Chrome (para ReservaUITest)

## Ejecutar

```bash
mvn spring-boot:run
```

Abrir http://localhost:8080/citas.html

## Pruebas

```bash
mvn test
mvn verify
mvn -Dtest=ReservaUITest -DexcludedGroups= test
```

Cobertura: `target/site/jacoco/index.html`

## API

`POST /api/citas` → 201 creada, 400 inválida, 409 conflicto.
