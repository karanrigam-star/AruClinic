# AruClinic Project Guidelines

This file contains instructions for building, running, and contributing to the **AruClinic** application.

## Build & Test Commands
* **Compile Project:** `mvn clean compile`
* **Compile & Run Tests:** `mvn test`
* **Start Dev Server:** `mvn spring-boot:run`

## Project Layout
* **Backend:** Spring Boot (Java 21) in `src/main/java`
* **Frontend:** Vaadin 24 in `src/main/java/com/aruclinic/frontend/views`
* **Styles & Assets:** CSS style sheets in `frontend/themes/aruclinic/`
* **Database Migrations:** Flyway scripts in `src/main/resources/db/migration`

## Development Guidelines
* **Lombok & IDE Compatibility:** Always implement manual getters and setters for newly added or critical entity fields (in addition to Lombok annotations) to prevent compiler and IDE annotation-processor warnings (e.g. `getId() undefined`).
* **Vaadin Routing:**
  * Do not annotate layout wrapper classes implementing `RouterLayout` (such as `MainLayout.java`) with `@Route`.
  * Map root route `""` using a redirection view (like `HomeView.java`) that routes users based on active session and roles.
* **Database Constraint Safety:** Use `@Transient` and DTO fields for UI state data rather than altering the schema files directly without a validated database migration script.
