import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("org.jlleitschuh.gradle.ktlint")
    id("io.gitlab.arturbosch.detekt")
    id("com.github.ben-manes.versions")
}

group = "edu.berkeley.cs"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        url = uri("https://kotlin.bintray.com/kotlinx")
    }
}


dependencies {
    // JUnit
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.2")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "15"
}

tasks.test {
    useJUnitPlatform()
}

tasks.getByPath("detekt").onlyIf { gradle.startParameter.taskNames.contains("detekt") }


ktlint {
    version.set("0.41.0")

    filter {
        exclude("build/**")
        exclude("**/generated/**")
        /*
        From https://github.com/jlleitschuh/ktlint-gradle#faq:
        Gradle based filtering are only working for files located inside project (subproject) folder, see https://github.com/gradle/gradle/issues/3417 To filter files outside project dir, use:
         */
        exclude { element -> element.file.path.contains("generated/") }
    }
}

detekt {
    toolVersion = "1.16.0"
    config = files("${rootDir}/detekt.yml")
    buildUponDefaultConfig = true
}

// documented at https://github.com/ben-manes/gradle-versions-plugin
tasks.named<DependencyUpdatesTask>("dependencyUpdates").configure {
    gradleReleaseChannel = "current" // rather than nightly or release-candidate (the default)
    rejectVersionIf {
        listOf("-m", "-rc", "-alpha", "-beta").any { it in candidate.version.toLowerCase() }
    }
}
