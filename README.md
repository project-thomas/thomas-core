# 🚀 T.H.O.M.A.S. CORE

[![Kotlin](https://img.shields.io/badge/kotlin-2.1-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![JVM](https://img.shields.io/badge/JVM-21-orange.svg)](https://openjdk.java.net/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)](https://github.com/NicoBondarenco/thomas-core)
[![Coverage](https://img.shields.io/badge/coverage-95%25-brightgreen.svg)](https://github.com/NicoBondarenco/thomas-core)
[![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)](https://github.com/NicoBondarenco/thomas-core/releases)

**T.H.O.M.A.S. means Tool for Handling Operations, Management, and Assistance Strategies**

**And it's also my son's name.**

The aim of this project is to be a P.O.C. (Proof of Concept), a clean architecture study, where any framework can be used for information I/O, and it is not necessary to rewrite any business rules, just the plugins of the frameworks that were used.

This module holds the core functionalities for all other projects, such as session context, roles, security entities, common extensions, some base classes and other things.

## 📋 Table of Contents

- [About the Project](#about-the-project)
- [Features](#features)
- [Requirements](#requirements)
- [Usage](#usage)
- [Testing](#testing)
- [Contributing](#contributing)
- [License](#license)

## 🎯 About the Project

**T.H.O.M.A.S. Core** is the foundational module of a clean architecture Kotlin ecosystem. It focuses on domain-centric design, strong boundaries, and framework independence. You can plug different adapters (HTTP, messaging, persistence, etc.) without rewriting business rules.

Key goals:
- Framework-agnostic, portable domain
- High testability and reproducibility
- Strong separation of concerns
- Safe concurrency with coroutines

## ✨ Features

- 🏗️ Base entities with validation
- 🔐 Flexible authorization with roles and permissions
- 🌐 Thread-safe session context with coroutine propagation
- 🎨 Kotlin extensions for common operations
- 🌍 Internationalization (i18n) support
- 📊 Aspect-oriented capabilities for logging/auditing
- 🔄 Asynchronous programming with Kotlin Coroutines
- 🧩 Clean Architecture and SOLID principles

## ✅ Requirements

- JDK 21
- Kotlin 2.2.0
- Gradle 8.14.3

## Usage

```kotlin
plugins {
    id("io.freefair.aspectj.post-compile-weaving") version "8.14.2"
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://repo.repsy.io/mvn/<REPSY_USERNAME>/thomas-release")
        credentials {
            username = "<REPSY_USERNAME>"
            password = "<REPSY_PASSWORD>"
        }
    }
}

dependencies {
    implementation("com.thomas:thomas-core:1.0.0")
    implementation("org.aspectj:aspectjrt:1.9.24")

    aspect("com.thomas:thomas-core:1.0.0")
    aspect("org.aspectj:aspectjweaver:1.9.24")

    testImplementation("com.thomas:thomas-core:1.0.0:test-fixtures")
    testFixturesImplementation("com.thomas:thomas-core:1.0.0:test-fixtures")
}

tasks.withType<KotlinCompile> {
    configure<AjcAction> {
        enabled = true
        classpath
        options {
            aspectpath.setFrom(configurations.aspect)
            compilerArgs = listOf(
                "-showWeaveInfo",
                "-verbose",
                "-XnoInline",
                "-Xlint:adviceDidNotMatch=ignore"
            )
        }
    }
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        freeCompilerArgs.addAll(
            "-Xjsr305=strict",
            "-Xcontext-receivers",
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-java-parameters",
            "-Xconcurrent-gc"
        )
    }
}

tasks.named("compileTestKotlin", KotlinCompile::class) {
    configure<AjcAction> {
        enabled = true
        options {
            aspectpath.setFrom(configurations.aspect)
            aspectpath.from("${layout.buildDirectory.get()}/classes/kotlin/main")
            compilerArgs = listOf(
                "-showWeaveInfo",
                "-verbose",
            )
        }
    }
}

```

## Testing
```shell
./gradlew clean build koverVerify koverXmlReport koverHtmlReport
```

## Contributing

The project is still in its early stages, so contributions are welcome.
It's using git workflow, so you can fork the project and create a pull request.

## License

The project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.
