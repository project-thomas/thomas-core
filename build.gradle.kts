import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit
import kotlinx.kover.gradle.plugin.dsl.GroupingEntityType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.lang)
    alias(libs.plugins.test.fixtures)
    id("org.jetbrains.kotlinx.kover") version "0.9.1"
    id("org.sonarqube") version "6.2.0.5505"
}

group = "com.thomas"
version = "0.0.1"

java.sourceCompatibility = JavaVersion.valueOf(libs.versions.target.get())
java.targetCompatibility = JavaVersion.valueOf(libs.versions.target.get())

kotlin {
    jvmToolchain(libs.versions.jdk.get().toInt())
}

repositories {
    mavenCentral()
    mavenLocal()
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
                    groupBy = GroupingEntityType.APPLICATION
                    bound {
                        aggregationForGroup = AggregationType.COVERED_PERCENTAGE
                        coverageUnits = CoverageUnit.BRANCH
                        minValue = 95
                    }
                }
                rule("Line Coverage of Tests must be more than 95%") {
                    disabled = false
                    groupBy = GroupingEntityType.APPLICATION
                    bound {
                        aggregationForGroup = AggregationType.COVERED_PERCENTAGE
                        coverageUnits = CoverageUnit.LINE
                        minValue = 95
                    }
                }
                rule("Instruction Coverage of Tests must be more than 95%") {
                    disabled = false
                    groupBy = GroupingEntityType.APPLICATION
                    bound {
                        aggregationForGroup = AggregationType.COVERED_PERCENTAGE
                        coverageUnits = CoverageUnit.INSTRUCTION
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
        property("sonar.sources",  file("$projectDir/src/main/kotlin/"))
        property("sonar.tests", file("$projectDir/src/test/kotlin/"))
        property("sonar.projectName", "T.H.O.M.A.S. Core")
        property("sonar.projectKey", "thomas-core")
        property("sonar.login", System.getenv("THOMAS_CORE_SONAR_LOGIN"))
        property("sonar.host.url", System.getenv("THOMAS_CORE_SONAR_URL"))
        property("sonar.coverage.jacoco.xmlReportPaths", "${layout.buildDirectory.get()}/reports/kover/reportJvm.xml")
        property("sonar.verbose", true)
    }
}

tasks.withType<KotlinCompile> {
    compilerOptions.jvmTarget.set(JvmTarget.valueOf(libs.versions.jvm.get()))
}

tasks.named("sonar") {
    dependsOn("koverXmlReport")
}
