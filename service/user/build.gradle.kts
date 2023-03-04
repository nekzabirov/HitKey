group = "com.hitkey"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")

    runtimeOnly("org.postgresql:r2dbc-postgresql")

    implementation("org.springframework.boot:spring-boot-starter-security")

    implementation("io.jsonwebtoken:jjwt-api:0.11.2")
    providedRuntime("io.jsonwebtoken:jjwt-impl:0.11.2")
    providedRuntime("io.jsonwebtoken:jjwt-jackson:0.11.2")
}
