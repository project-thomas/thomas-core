import com.thomas.project.task.versioning.currentVersion
import java.net.URI
import kotlinx.kover.gradle.plugin.dsl.AggregationType.COVERED_PERCENTAGE
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit.BRANCH
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit.INSTRUCTION
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit.LINE
import kotlinx.kover.gradle.plugin.dsl.GroupingEntityType.APPLICATION
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    alias(libs.plugins.kotlin.lang)
    alias(libs.plugins.test.fixtures)
    alias(libs.plugins.kotlinx.kover)
    alias(libs.plugins.sonarqube.scanner)
    alias(libs.plugins.project.plugin)
    `maven-publish`
    java
}

group = "com.thomas"
version = project.currentVersion

java.sourceCompatibility = JavaVersion.valueOf(libs.versions.target.get())
java.targetCompatibility = JavaVersion.valueOf(libs.versions.target.get())

kotlin {
    jvmToolchain(libs.versions.jdk.get().toInt())
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.valueOf(libs.versions.jvm.get()))
        freeCompilerArgs.addAll(
            "-Xjsr305=strict",
            "-Xcontext-receivers",
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
        )
    }
}

dependencies {
    implementation(libs.bundles.kotlin.stdlib.all)
    implementation(libs.bundles.kotlinx.coroutines.all)
    implementation(libs.bundles.log.logback.all)
    implementation(libs.bundles.aspectj.all)

    testImplementation(libs.bundles.junit.all)
}

tasks.test {
    useJUnitPlatform()
    systemProperty("file.encoding", "UTF-8")
    systemProperty("user.timezone", "UTC")
    maxHeapSize = "1g"

    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

kover {
    currentProject {
        sources {
            excludedSourceSets.addAll("test", "testFixtures")
        }
    }
    reports {
        filters {
            excludes {
                classes(
                    "com.thomas.core.model.security.SecurityRole*",
                    "com.thomas.core.model.security.SecurityRoleGroup*",
                    "com.thomas.core.model.security.SecurityRoleSubgroup*",
                )
            }
        }
        total {
            verify {
                onCheck = false
                rule("Branch Coverage of Tests must be more than 95%") {
                    disabled = false
                    groupBy = APPLICATION
                    bound {
                        aggregationForGroup = COVERED_PERCENTAGE
                        coverageUnits = BRANCH
                        minValue = 95
                    }
                }
                rule("Line Coverage of Tests must be more than 95%") {
                    disabled = false
                    groupBy = APPLICATION
                    bound {
                        aggregationForGroup = COVERED_PERCENTAGE
                        coverageUnits = LINE
                        minValue = 95
                    }
                }
                rule("Instruction Coverage of Tests must be more than 95%") {
                    disabled = false
                    groupBy = APPLICATION
                    bound {
                        aggregationForGroup = COVERED_PERCENTAGE
                        coverageUnits = INSTRUCTION
                        minValue = 95
                    }
                }
            }
            xml {
                onCheck = false
            }
            html {
                onCheck = false
            }
        }
    }
}

sonar {
    properties {
        property("sonar.sources", file("$projectDir/src/main/kotlin/"))
        property("sonar.tests", file("$projectDir/src/test/kotlin/"))
        property("sonar.projectName", "T.H.O.M.A.S. Core")
        property("sonar.projectKey", "thomas-core")
        property("sonar.login", System.getenv("THOMAS_CORE_SONAR_LOGIN"))
        property("sonar.host.url", System.getenv("THOMAS_SONAR_URL"))
        property("sonar.coverage.jacoco.xmlReportPaths", "${layout.buildDirectory.get()}/reports/kover/report.xml")
        property("sonar.verbose", true)
        property("sonar.qualitygate.wait", true)
        property(
            "sonar.exclusions", listOf(
                "**/com/thomas/core/model/security/SecurityRole**",
                "**/com/thomas/core/model/security/SecurityRoleGroup**",
                "**/com/thomas/core/model/security/SecurityRoleSubgroup**",
            ).joinToString(separator = ",")
        )
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
            from(components["kotlin"])
            artifact(tasks.named("testFixturesJar")) {
                classifier = "test-fixtures"

            }
            pom {
                packaging = "jar"
                name.set("T.H.O.M.A.S. Core")
                description.set("T.H.O.M.A.S. Core module for use in all other modules of the project.")
                licenses {
                    license {
                        name.set("MIT license")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        name.set("Nicanor Bondarenco")
                        email.set("nicanor_bondarenco@hotmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/NicoBondarenco/thomas-core.git")
                    developerConnection.set("scm:git:ssh://github.com/NicoBondarenco/thomas-core.git")
                    url.set("https://github.com/NicoBondarenco/thomas-core")
                }

            }
        }
    }
    repositories {
        maven {
            val repository = if (project.version.toString().endsWith("SNAPSHOT")) {
                "snapshot"
            } else {
                "release"
            }
            url = URI.create("https://repo.repsy.io/mvn/${System.getenv("REPSY_USERNAME")}/thomas-$repository")
            credentials {
                username = System.getenv("REPSY_USERNAME")
                password = System.getenv("REPSY_PASSWORD")
            }
        }
    }
}
