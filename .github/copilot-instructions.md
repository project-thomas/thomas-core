# GitHub Copilot Instructions for T.H.O.M.A.S. Core

## Project Overview

You are working on T.H.O.M.A.S. Core, a Kotlin-based library project that serves as the foundational module for a larger enterprise application ecosystem. This project follows strict enterprise-grade standards with comprehensive testing, code coverage, and quality gates.

## Technology Stack

### Core Technologies
- **Language**: Kotlin 2.2.0 with JVM target 21
- **Build Tool**: Gradle 8.x with Kotlin DSL
- **Concurrency**: Kotlinx Coroutines 1.10.2 for asynchronous programming
- **Testing**: JUnit 5 (Jupiter) with extensive mocking capabilities
- **Logging**: Logback with Kotlin logging extensions
- **AOP**: AspectJ for cross-cutting concerns

### Key Dependencies
- Kotlin Standard Library (JDK8, Common, Reflect)
- Kotlinx Coroutines (Core, JVM, SLF4J, Reactor)
- Logback Classic + Kotlin Logging
- AspectJ Runtime and Tools
- JUnit Jupiter with Pioneer extensions
- Mockito + MockK for testing
- System Stubs for testing environment variables

### Build & Quality Tools
- **Coverage**: Kotlinx Kover with 95% minimum coverage requirement
- **Quality Gate**: SonarQube integration with strict rules
- **Publishing**: Maven publishing to private repository (Repsy.io)
- **CI/CD**: GitHub Actions integration

## Project Structure

```
src/
├── main/kotlin/com/thomas/core/
│   ├── aspect/          # AOP aspects and cross-cutting concerns
│   ├── authorization/   # Authorization and security logic
│   ├── context/        # Application context management
│   ├── exception/      # Custom exception hierarchy
│   ├── extension/      # Kotlin extension functions
│   ├── i18n/          # Internationalization support
│   └── model/         # Domain models and data structures
│       ├── entity/    # Core entities
│       ├── general/   # General-purpose models
│       ├── pagination/# Pagination utilities
│       └── security/  # Security-related models
├── test/kotlin/       # Unit tests mirroring main structure
├── testFixtures/kotlin/ # Shared test utilities
└── resources/strings/  # Internationalization files
```

## Coding Standards & Best Practices

### Kotlin Style
- Use idiomatic Kotlin patterns and conventions
- Prefer immutable data structures with `data class`
- Leverage Kotlin's null safety features
- Use coroutines for asynchronous operations
- Apply functional programming principles where appropriate
- Utilize Kotlin's type system for domain modeling

### Architecture Patterns
- Clean Architecture principles
- Domain-driven design for model organization
- Aspect-oriented programming for cross-cutting concerns
- Dependency injection ready (preparation for Spring/DI frameworks)
- Context pattern for request/session management

### Testing Requirements
- **Coverage**: Minimum 95% for branches, lines, and instructions
- **Test Structure**: Mirror main source structure in test directories
- **Test Types**: Unit tests with comprehensive mocking
- **Test Fixtures**: Shared test utilities in testFixtures
- **Excluded Classes**: Security role models are excluded from coverage

### Code Quality Rules
- SonarQube quality gates must pass
- No commented code in production
- Comprehensive error handling with custom exceptions
- Proper logging at appropriate levels
- Thread-safe implementations for concurrent access

## Development Guidelines

### When Creating New Code
1. **Domain Models**: Place in appropriate model subdirectory
2. **Business Logic**: Use clean separation of concerns
3. **Error Handling**: Create specific exception types in exception package
4. **Logging**: Use kotlin-logging with appropriate log levels
5. **Testing**: Write tests first (TDD approach preferred)
6. **Documentation**: Focus on code clarity over comments

### When Working with Coroutines
- Use `suspend` functions for I/O operations
- Prefer structured concurrency patterns
- Handle cancellation properly
- Use appropriate dispatchers (IO, Default, Main)
- Test with `kotlinx-coroutines-test`

### When Adding Dependencies
- Update `libs.versions.toml` with version catalogs
- Group related dependencies in bundles
- Justify new dependencies in terms of project needs
- Ensure compatibility with existing stack

### Code Organization
- **Extensions**: Generic utility extensions in extension package
- **Aspects**: Cross-cutting concerns like logging, security in aspect package
- **Context**: Request/session context management
- **Authorization**: Security and permission logic
- **I18n**: Internationalization with property files in resources/strings

## Quality Gates

### Coverage Requirements
- Branch Coverage: ≥ 95%
- Line Coverage: ≥ 95%
- Instruction Coverage: ≥ 95%

### Excluded from Coverage
- `SecurityRole*` classes
- `SecurityRoleGroup*` classes  
- `SecurityRoleSubgroup*` classes

### SonarQube Integration
- All quality gates must pass before merge
- Code smells should be addressed
- Security hotspots must be reviewed
- Duplicated code should be refactored

## Publishing & Versioning

### Version Management
- Semantic versioning (MAJOR.MINOR.PATCH)
- SNAPSHOT versions for development
- Release versions for stable builds
- Custom Gradle tasks for version increment

### Publishing Strategy
- Maven publishing to private repository
- Test fixtures published as separate artifact
- Source and Javadoc jars included
- Proper POM metadata with licensing

## IDE Integration

### IntelliJ IDEA Setup
- Kotlin plugin with latest version
- Code style configured for project standards
- Run configurations for tests and coverage
- SonarLint integration for real-time analysis

## Security Considerations

- No hardcoded credentials or secrets
- Environment variables for configuration
- Secure handling of sensitive data
- Authorization models with proper abstraction
- Security aspects for audit trails

## Performance Guidelines

- Leverage Kotlin's performance optimizations
- Use coroutines for non-blocking operations
- Implement proper caching strategies where needed
- Monitor memory usage in data structures
- Profile critical paths for optimization

## Documentation Standards

- Clear, self-documenting code
- Comprehensive README for module usage
- API documentation for public interfaces
- Architecture decision records for major choices
- Migration guides for breaking changes

Remember: This is a foundational library that other modules depend on. Maintain backward compatibility, ensure thread safety, and prioritize reliability over features.
