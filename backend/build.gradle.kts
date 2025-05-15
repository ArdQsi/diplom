plugins {
    kotlin("jvm") version "1.9.25"
    id("org.jetbrains.kotlin.plugin.spring") version "1.9.21" // Для Spring, если используете
    id("org.springframework.boot") version "3.3.8"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.newdex"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.springframework.boot:spring-boot-starter-web") // Для Spring Boot web

    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")

//    Для чтения конфигурации
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    implementation("io.jsonwebtoken:jjwt-impl:0.11.5")
    implementation("io.jsonwebtoken:jjwt-jackson:0.11.5")

    // Другие зависимости
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testImplementation("io.mockk:mockk:1.13.4")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "17" // Или более свежая версия
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
    filter {
        includeTestsMatching("*Test") // Или конкретное имя класса
        includeTestsMatching("*.*") // Для всех тестов
    }
}