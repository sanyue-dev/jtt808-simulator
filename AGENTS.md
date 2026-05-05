# Repository Guidelines

## Project Structure & Module Organization

This is a Java 17 Spring Boot JT/T 808 simulator. Main code lives in `src/main/java/cn/org/hentai/simulator`:

- `app/` contains the application entry point, `SimulatorApp`.
- `domain/` contains entities, in-memory models, and enums.
- `infrastructure/` contains MyBatis persistence and shared utilities.
- `engine/` contains simulation runtime code: tasks, events, Netty connections, runners, and logs.
- `service/` contains business services and managers.
- `web/` contains Spring MVC controllers and view objects.

Runtime resources live in `src/main/resources`. FreeMarker pages are in `templates/`, static CSS/JS/images are in `static/`, database initialization is in `schema.sql` and `data.sql`, and configuration is in `application.yml`. Project images and documentation assets live in `doc/`.

## Build, Test, and Development Commands

Use the Maven wrapper when possible:

```bash
./mvnw clean package
./mvnw spring-boot:run
java -jar target/jtt808-simulator-1.0-SNAPSHOT.jar
```

`clean package` compiles and packages the Spring Boot jar. `spring-boot:run` starts the local web application on port `18888`. Running the jar uses the packaged artifact from `target/`.

Prerequisites: JDK 17, MySQL with a `simulator` database, and a JT/T 808 server endpoint matching `application.yml` defaults.

## Coding Style & Naming Conventions

Follow the existing Java style: 4-space indentation, UTF-8, package names in lowercase, classes in `PascalCase`, methods and fields in `camelCase`, and constants in `UPPER_SNAKE_CASE`. Keep controllers thin; put business behavior in `service/` or `engine/`. FreeMarker templates use `.ftlh`; static assets should stay under the matching `static/css`, `static/js`, or `static/img` folder.

## Testing Guidelines

There is currently no `src/test` tree. Add tests under `src/test/java` using Maven/Spring Boot conventions, mirroring the package under test. Name unit tests `*Test` and integration tests `*IT` when added. Run all tests with:

```bash
./mvnw test
```

Prefer tests that expose protocol, task lifecycle, route generation, and persistence failures directly instead of hiding them behind silent fallbacks.

## Commit & Pull Request Guidelines

Recent history uses Conventional Commit-style messages, often in Chinese, for example `fix(UI): ...`, `refactor(UI): ...`, and `chore: ...`. Keep commits focused and use a clear type such as `fix`, `feat`, `refactor`, `test`, or `chore`.

Pull requests should include a short summary, affected modules or pages, validation commands run, linked issues when available, and screenshots for visible UI changes.

## Agent-Specific Instructions

Do not add silent fallback paths, mock success behavior, or arbitrary guardrails just to make execution pass. Surface failures clearly with explicit errors, logs, or failing tests so root causes can be fixed.

## Agent skills

### Issue tracker

Issues and PRDs live in this repo's GitHub Issues. See `docs/agents/issue-tracker.md`.

### Triage labels

Use the default triage label vocabulary: `needs-triage`, `needs-info`, `ready-for-agent`, `ready-for-human`, and `wontfix`. See `docs/agents/triage-labels.md`.

### Domain docs

This is a single-context repo: read root `CONTEXT.md` and `docs/adr/` when present. See `docs/agents/domain.md`.
