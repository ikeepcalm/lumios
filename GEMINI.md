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
- **AI Assistant**: Conversational capabilities powered by Gemini and OpenAI.

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
- **Persistence:** Use the provided `Service` interfaces (e.g., `UserService`, `ChatService`) instead of accessing repositories directly in handlers.
