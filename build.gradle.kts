import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

task("clean") {
    group = "build"
    description = "Deletes the build directory for all subprojects"

    delete(File(rootDir, ".gradle"))

    delete(buildDir)

    subprojects {
        delete(buildDir)

        delete(File(".gradle"))
    }

    gradle.includedBuilds.forEach { build ->
        delete(File(build.projectDir, "build"))
    }
}

plugins {
    id("org.springframework.boot") version "3.0.2" apply false
    id("io.spring.dependency-management") version "1.1.0" apply false
    kotlin("jvm") version "1.7.22" apply false
    kotlin("plugin.spring") version "1.7.22" apply false
}

repositories {
    mavenCentral()
}

subprojects {
    plugins.apply("org.springframework.boot")
    plugins.apply("io.spring.dependency-management")
    plugins.apply("org.jetbrains.kotlin.jvm")
    plugins.apply("org.jetbrains.kotlin.plugin.spring")

    repositories {
        mavenCentral()
        maven { url = uri("https://artifactory-oss.prod.netflix.net/artifactory/maven-oss-candidates") }
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "17"
        }
    }

    dependencies {
        add("implementation", "org.jetbrains.kotlin:kotlin-reflect")
        add("implementation", "org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        add("implementation", "com.fasterxml.jackson.module:jackson-module-kotlin")
        add("implementation", "io.projectreactor.kotlin:reactor-kotlin-extensions")
        add("implementation", "org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    }
}