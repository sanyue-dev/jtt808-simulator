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

Database tables are created on application startup by `schema.sql` using `CREATE TABLE if not exists`. The default JT/T 808 server endpoint is `127.0.0.1:20021`.

## Architecture

JT/T 808 protocol terminal simulator. Simulated vehicles connect to a JT/T 808 server over Netty TCP, report locations, and handle downlink commands. The project is intended for stress testing up to 100,000 concurrent online vehicles.

### Package Layering

```text
cn.org.hentai.simulator
├── domain/                # Domain layer: data only, no business logic
│   ├── entity/            # Database entities: Route, RoutePoint, StayPoint, etc.
│   ├── model/             # In-memory business models: DrivePlan, Point, TaskInfo, XRoute, etc.
│   └── enums/             # Enums: TaskState, TaskStatus, LogType
├── infrastructure/        # Infrastructure layer
│   ├── persistence/       # Persistence
│   │   ├── mapper/        # MyBatis Mapper interfaces
│   │   └── example/       # MyBatis Example query classes
│   └── util/              # Utilities
├── service/               # Service layer: business logic
│   ├── RouteManager       # Route cache and drive plan generation
│   ├── TaskManager        # Task lifecycle management
│   ├── ScheduleTaskManager # Scheduled task management
│   └── *Service           # CRUD services
├── web/                   # Web layer
│   ├── controller/        # Spring MVC controllers
│   └── vo/                # View objects: Page, Result
├── engine/                # Simulation engine
│   ├── core/              # AbstractDriveTask, SimpleDriveTask
│   ├── event/             # Event system: EventDispatcher, @Listen
│   ├── net/               # Netty connection pool: ConnectionPool
│   ├── runner/            # Thread pool: RunnerManager
│   └── log/               # Engine logs
└── app/                   # Application entry point: SimulatorApp
```

Dependency direction is a DAG: `domain <- infrastructure <- engine <- service <- web`.

### Core Simulation Engine

The engine uses an event-driven model plus EventLoop-style scheduling:

- **AbstractDriveTask**: base class for simulation tasks. Lifecycle methods are `init()` then `startup()`. The state machine is idle -> driving -> parking -> terminated.
- **SimpleDriveTask**: concrete JT/T 808 implementation: protocol communication, terminal registration/authentication, and T0200 location reporting every 5 seconds.
- **EventDispatcher** and `@Listen`: event routing and dispatch. The `attachment` value supports secondary routing by message ID.
- **RunnerManager**: `ScheduledExecutorService`-based scheduling for event callbacks and delayed/periodic tasks.
- **ConnectionPool**: Netty client connection pool that handles JT/T 808 encode/decode.
- **TaskState**: idle -> driving -> parking -> terminated.

### Route & Track

- **RouteManager** loads routes, maintains route cache, and generates `DrivePlan` instances.
- Track randomization is intentional: each drive can use a different track shape while following the same route. Do not force all devices through identical paths via OSRM or similar routing engines.
- Route data consists of track points (`RoutePoint`), stay points (`StayPoint`), and trouble segments (`TroubleSegment`).
- Coordinates are WGS84 throughout the project.

### Web Layer

Spring Boot + MyBatis + FreeMarker templates. The frontend uses jQuery, Bootstrap, Leaflet, and OpenStreetMap.

| Controller | Path | Responsibility |
|---|---|---|
| RouteController | `/route/*` | Route CRUD, track points, and stay points |
| TaskController | `/task/*` | Create and start a single simulation task |
| BatchController | `/batch/*` | Batch task creation for stress testing |
| MonitorController | `/monitor/list/*` | Real-time task status monitoring |
| MapMonitorController | `/monitor/*` | Map-based real-time track monitoring |

Database entities use the MyBatis Generator Example pattern, such as `RouteExample`. Mapper XML files live in `src/main/resources/cn/org/hentai/simulator/infrastructure/persistence/mapper/`.

### Database Tables

- `x_route`: route metadata, speed range, mileage, and station JSON.
- `x_route_point`: route track points with longitude and latitude.
- `x_stay_point`: stay points with longitude, latitude, stay duration range, and trigger probability.
- `x_trouble_segment`: trouble segments with start/end indexes, event code, and trigger probability.
- `x_schedule_task`: scheduled trip tasks.

### Protocol

The project uses `org.yzh:jtt808-protocol` for JT/T 808 encoding and decoding. Supported message types include T0100 registration, T0102 authentication, T0001 terminal common response, T0200 location report, T8100 registration response, and T8300 text downlink. Location reports use WGS84 coordinates directly.

### Extending Server Message Handlers

Add methods to `SimpleDriveTask` or a custom subclass and annotate them with `@Listen`:

```java
@Listen(when = EventEnum.connected)
public void onConnected() { ... }

@Listen(when = EventEnum.message_received, attachment = "8801")
public void onCameraCaptureCommand(JTT808Message msg) { ... }
```

Delayed and periodic task APIs are defined on `AbstractDriveTask`:

- `executeAfter(Executable, milliseconds)`: delayed execution.
- `executeConstantly(Executable, interval)`: periodic execution.

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

## Key Conventions

- FreeMarker pages use `.ftlh`.
- Centralized configuration lives in `application.yml`, including database and vehicle server settings.
- `simulator.mode` configures the simulation mode; the current mode is `stress`.
- After changing JS/CSS under `static/`, browser caches may still serve old files. Verify with a hard refresh or DevTools `ignoreCache`.
- `TaskController`'s create page template is `task-create.ftlh`, and its URL path is `/task/index`, not `/task/create`.
- Follow `.claude/rules/frontend/ui-components.md` and `.claude/rules/frontend/map-pages.md` when touching matching frontend areas.

## Agent skills

### Issue tracker

Issues and PRDs live in this repo's GitHub Issues. See `docs/agents/issue-tracker.md`.

### Triage labels

Use the default triage label vocabulary: `needs-triage`, `needs-info`, `ready-for-agent`, `ready-for-human`, and `wontfix`. See `docs/agents/triage-labels.md`.

### Domain docs

This is a single-context repo: read root `CONTEXT.md` and `docs/adr/` when present. See `docs/agents/domain.md`.
