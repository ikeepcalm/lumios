# GEMINI.md - Project Context: Lumios

## Project Overview
Lumios is a sophisticated Telegram bot and backend application built with **Spring Boot 3**. It serves as a multi-functional assistant for group chats and private users, offering features like queue management, timetable tracking, task scheduling, and an AI-driven assistant.

### Main Technologies
- **Framework:** Spring Boot 3.3.0 (Java 21)
- **Telegram API:** `telegrambots-longpolling` (version 9.0.0)
- **Database:** MariaDB/MySQL with Spring Data JPA
- **AI Integrations:** Google Gemini and OpenAI
- **Security:** Spring Security
- **Caching:** Caffeine
- **Documentation:** SpringDoc OpenAPI (Swagger)
- **Build Tool:** Gradle

## Architecture and Structure

### Package Structure: `dev.ua.ikeepcalm.lumios`
- `.database`: Contains DAL (Data Access Layer), entities (queues, records, reverence, tasks, timetable), and repositories.
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
- **Task Tracker**: Track deadlines and tasks with `/task` and `/due`.
- **Reverence System**: A social "respect" system where users gain/lose points based on message reactions.
- **AI Assistant**: Conversational capabilities powered by Gemini and OpenAI.

## Building and Running

### Development
- **Build:** `./gradlew build`
- **Run:** `./gradlew bootRun`
- **Tests:** `./gradlew test` (Note: Ensure database and environment variables are configured).

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
