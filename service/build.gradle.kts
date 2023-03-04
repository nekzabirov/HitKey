val bootWarAll by tasks.registering {
    group = "application"
    description = "Builds a WAR archive of all sub-projects."

    subprojects {
        dependsOn(this.tasks.getByName("bootWar"))
    }
}

extra["springCloudVersion"] = "2022.0.1"

subprojects {
    plugins.apply("org.gradle.war")

    dependencyManagement {
        imports {
            mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
        }
    }

    dependencies {
        add("implementation", "org.springframework.boot:spring-boot-starter-webflux")
        add("providedRuntime", "org.springframework.boot:spring-boot-starter-tomcat")

        add("testImplementation", "org.springframework.boot:spring-boot-starter-test")

        testImplementation("io.projectreactor:reactor-test")
        testImplementation("org.springframework.security:spring-security-test")

        implementation(project(":common"))

        implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}