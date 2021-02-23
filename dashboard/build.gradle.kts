repositories {
    mavenCentral()
    jcenter()
}

plugins {
    kotlin("js")
    // id("kotlin2js") version "1.4.30"
    id("org.jlleitschuh.gradle.ktlint")
    id("io.gitlab.arturbosch.detekt")
    id("com.github.ben-manes.versions")
}

kotlin {
    js(IR) {
        browser {
            binaries.executable()
        }
    }
}

val kotlinReactVersion = "17.0.1-pre.148-kotlin-1.4.30"
val reactVersion = "17.0.1"

dependencies {
    // implementation(kotlin("stdlib-js"))

    implementation("org.jetbrains:kotlin-react:$kotlinReactVersion")
    implementation("org.jetbrains:kotlin-react-dom:$kotlinReactVersion")
    implementation(npm("react", reactVersion))
    implementation(npm("react-dom", reactVersion))

    testImplementation(kotlin("test-js"))
}
