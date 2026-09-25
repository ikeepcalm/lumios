# GEMINI.md - Project Context: Lumios

## Project Overview
Lumios is a sophisticated Telegram bot and backend application built with **Spring Boot 3**. It serves as a multi-functional assistant for group chats and private users, offering features like queue management, timetable tracking, task scheduling, and an AI-driven assistant.

### Main Technologies
- **Framework:** Spring Boot 3.3.0 libraries on Java 25
- **Telegram API:** `telegrambots-longpolling` (version 9.0.0)
- **Database:** MariaDB/MySQL with Spring Data JPA
- **AI Integrations:** Google Gemini and OpenAI
- **Security:** Spring Security
- **Caching:** Caffeine
- **Documentation:** SpringDoc OpenAPI (Swagger)
- **Build Tool:** Gradle 9 (wrapper), Spring Boot Gradle plugin 3.5.6

## Architecture and Structure

### Package Structure: `dev.ua.ikeepcalm.lumios`
- `.database`: Contains DAL (Data Access Layer), entities (queues, records, reverence, timetable), and repositories.
- `.telegram`: Core bot logic.
    - `.core`: Custom annotations (`@BotCommand`, `@BotCallback`, `@BotReaction`, etc.) and interaction shortcuts.
    - `.interactions`: Implementation of commands, callbacks, and inline queries.
    - `.ai`: Services for Gemini and OpenAI integration.
    - `.scheduled`: Background tasks (e.g., cleanup, notifications).
    - `.utils`: Helpers for formatting, validation, and parsing.
- `.web`: REST API endpoints and security configuration.

### Key Components
- **`UpdateConsumer.java`**: The central dispatcher for all incoming Telegram updates. It uses reflection to find and invoke handlers based on custom annotations.
- **`ServicesShortcut.java`**: A base class for interaction handlers that provides easy access to all database services and the Telegram client.
- **`LumiosUser` / `LumiosChat`**: Core entities representing the bot's users and the chats they inhabit.

## Features
- **Queue Management**: `/queue`, `/mixed` commands to manage ordered lists of users.
- **Timetable**: Integration with a web-based editor to track and notify about classes/events. Commands: `/today`, `/tomorrow`, `/week`, `/now`, `/next`.
- **Workload estimate**: `/due` feeds the caller's own timetable for this week and the next to Gemini
  and asks what they probably have to prepare. There is no stored task list any more - the
  hand-written one went unused, and the shape of the fortnight (a lab has to be finished before the
  class it is defended at, a lecture needs nothing) carries most of the answer on its own.
- **Reverence System**: A social "respect" system where users gain/lose points based on message reactions.
- **Schedule Mini App**: `/app` (aliased as `/editor`) links to the timetable Mini App with
  `https://t.me/<bot>/<app>?startapp=<chatId>`. It must be a plain URL button - Telegram rejects
  `web_app` buttons outside private chats, so `startapp` is the only way the group id reaches the
  app. The frontend reads it from `Telegram.WebApp.initDataUnsafe.start_param` and sends it as the
  `chatId` header.
- **Personal timetables**: A group timetable imported from campus is a superset - it contains every
  elective (факультатив) on offer. `ElectiveDetector` finds elective pools structurally (any time
  slot holding more than one distinct subject), members pick theirs with `/mine`, and personal
  reminders are sent privately containing only their classes. Choices key on the subject name, not
  the class row, because `/import` deletes and recreates every `ClassEntry`. Once a member has
  picked anything at all, an elective pool they picked nothing from is hidden from them, so a day
  made only of other people's electives correctly reads as having no classes. A subgroup split is
  the exception - everybody attends one half, so an undecided member keeps seeing both.

## Building and Running

### Development
- **Build:** `./gradlew build`
- **Run:** `./gradlew bootRun`
- **Tests:** `./gradlew test` (Note: Ensure database and environment variables are configured).

### Build setup - things that look wrong but are not
The toolchain is Java 25 on Gradle 9, and three of the pieces below exist only because of that. None
of them should be "tidied up" back to the obvious form.
- **No `io.spring.dependency-management` plugin.** It mutates configuration attributes lazily and
  cannot run on Gradle 9 at all (`cannot mutate the dependency attributes of configuration
  ':compileOnly'`). The `platform("org.springframework.boot:spring-boot-dependencies")` dependency
  manages exactly the same versions and is Spring's own documented replacement.
- **The Spring Boot Gradle plugin (3.5.6) is ahead of the Spring Boot libraries (3.3.0).** 3.3.0's
  `bootJar` calls `CopyProcessingSpec.getDirMode()`, which Gradle 9 removed. The plugin only lays out
  the jar, so the skew is safe. Upgrading the libraries is a separate job - springdoc 2.4.0 does not
  go past Spring Framework 6.1.
- **No `thin-launcher` plugin.** It is unmaintained and its `thinPom` task uses `JavaPluginConvention`,
  gone in Gradle 9. `bootJar` now produces an ordinary fat jar, which is what the container wants
  anyway - it no longer resolves dependencies over the network at startup.
- Lombok must stay at 1.18.42 or newer; 1.18.30 throws `ExceptionInInitializerError` on JDK 25.
- Gradle 9 no longer supplies `junit-platform-launcher` on the test runtime classpath, hence the
  explicit `testRuntimeOnly`.

### Database migrations
The schema is owned by **Liquibase** (`src/main/resources/db/changelog`), not by Hibernate.
`spring.jpa.hibernate.ddl-auto=none` - never turn it back to `update`.

**Identifiers in changelogs must be snake_case.** Spring Boot 3.3 applies
`CamelCaseToUnderscoresNamingStrategy`, so `@Entity(name = "classEntries")` is really the table
`class_entries` and `reminderLeadMinutes` is really `reminder_lead_minutes`. Writing the entity or
field name verbatim produces a migration that fails on the real database (or, worse, quietly adds a
column nothing reads). When regenerating DDL from the entity model, configure
`CamelCaseToUnderscoresNamingStrategy` + `SpringImplicitNamingStrategy`, or the output is wrong.

- Master changelog: `db/changelog/db.changelog-master.yaml`, one included file per change.
- `001-baseline` recreates the pre-Liquibase schema on an empty database, and is marked as ran
  (not executed) where `chats` already exists, so existing deployments are untouched.
- Every changeset carries a `preConditions ... onFail: MARK_RAN` guard so it is safe to re-run
  against a database that already has the change.
- `ddl-auto=validate` is **not** usable yet: Hibernate maps `mixedQueues.id` as `uuid` when
  validating but emits `binary(16)` when creating it, so validation fails against a schema it
  generated itself. Fix that mapping before enabling validation.

### Configuration
The application requires several environment variables defined in `.env` (see `.env.example`):
- `MYSQL_URL`, `MYSQL_USER`, `MYSQL_PASSWORD`
- `TELEGRAM_TOKEN`, `TELEGRAM_USERNAME`
- `OPENAI_API_KEY`, `GEMINI_API_KEYS`
- `TENOR_API_KEY`

### Deployment
- **Docker:** `Dockerfile` and `docker-compose.yml` are provided.
- **Railway:** `railway.json` is present for Railway.app deployment.

## Development Conventions
- **Handlers:** New bot features should be implemented as `@Component` classes in the `interactions` package, implementing the `Interaction` interface and inheriting from `ServicesShortcut`.
- **Annotations:** Use the appropriate `@BotCommand`, `@BotCallback`, or `@BotReaction` annotation to route updates.
- **Formatting:** Use `MessageFormatter` and `MarkdownV2Sanitizer` to ensure Telegram messages are correctly escaped and formatted.
- **Time:** All timetable logic must go through `TimetableClock` (Europe/Kyiv), and any `@Scheduled`
  that reasons about class times must pass `zone = TimetableClock.ZONE_ID`. The container has no `TZ`
  set, so a bare cron or `LocalDate.now()` silently runs in UTC.
- **Translations:** Add every key to both `messages.properties` and `messages_en.properties`. A
  message that takes `{0}` arguments is run through `MessageFormat`, so a literal apostrophe in it
  must be doubled (`''`) or it disappears.
- **Web API auth:** `AuthenticationService` accepts either the `rest.api.header` API key or a
  Telegram `initData` string in `X-Telegram-Init-Data`. The initData path is deliberately limited to
  `GET /timetables/retrieve`: its signature proves the caller is a real Telegram user and that
  `start_param` was not altered in transit, but *not* that they belong to that chat - anyone can
  craft a startapp link for a chat id they know. Widening it to a write endpoint needs a membership
  check first (`LumiosUser` has a row per user-chat pair).
- **Persistence:** Use the provided `Service` interfaces (e.g., `UserService`, `ChatService`) instead of accessing repositories directly in handlers.
