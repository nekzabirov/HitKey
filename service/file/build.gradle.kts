group = "com.hitkey"
version = "1.0.0"
java.sourceCompatibility = JavaVersion.VERSION_17

dependencies {
    implementation("org.projectlombok:lombok")

    implementation("io.jsonwebtoken:jjwt-api:0.11.2")
    providedRuntime("io.jsonwebtoken:jjwt-impl:0.11.2")
    providedRuntime("io.jsonwebtoken:jjwt-jackson:0.11.2")

    implementation("org.springframework.boot:spring-boot-starter-security")
}