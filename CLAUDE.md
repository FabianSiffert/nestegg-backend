# nestegg-backend

Spring Boot backend for nestegg, a personal investment tracker. The developer is an experienced Android developer learning backend development; this project is primarily a learning project, built by pair programming.

## Read first

- `README.md`: tech stack, key decisions, how to run
- `TODO.md`: open tasks and ideas for later
- `docs/learnings.md`: concepts already explained
- `docs/inv-integration.md`: planned integration with inv, the Pokémon card browser

## Conventions

- Layered architecture: controller → service → repository. The JPA entity is the domain model; DTOs exist only at the API boundary.
- Package by feature under `io.siffert.nestegg.backend` (e.g. `asset/`), shared code in `common/`.
- Services return DTOs, never entities. Mapping happens inside the transaction via extension functions (`toEntity()`, `toResponse()`).
- Services use `@Transactional(readOnly = true)` on the class and `@Transactional` on writing methods.
- Not-found errors: feature-specific exceptions extending an abstract `NotFoundException` in `common/`, mapped to 404. Prefer explicit, readable error cases.
- Schema changes only via new Flyway migrations in `src/main/resources/db/migration`. Never edit an applied migration.
- Money and prices: `numeric` in SQL, `BigDecimal` in Kotlin. Prices are unit prices.
- Indentation: tabs.

## Stack notes

- Spring Boot 4 and Jackson 3: several packages moved compared to Boot 3 tutorials (e.g. `tools.jackson`, `org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest`). Check the actual jars when unsure.
- Docker Desktop must be running for `bootRun` (Docker Compose) and tests (Testcontainers).
