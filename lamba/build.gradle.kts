val bootWarAll by tasks.registering {
    group = "application"
    description = "Builds a WAR archive of all sub-projects."

    subprojects {
        dependsOn(this.tasks.getByName("bootWar"))
    }
}

extra["springCloudVersion"] = "2022.0.1"

subprojects {
    dependencyManagement {
        imports {
            mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
        }
    }
    plugins.apply("org.gradle.war")
    plugins.apply("org.jetbrains.kotlin.jvm")

    dependencies {
        implementation("org.springframework.cloud:spring-cloud-function-context")
        implementation("org.springframework.cloud:spring-cloud-starter-function-webflux")
        implementation("org.springframework.cloud:spring-cloud-starter-function-web")

        implementation(project(":common"))
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}