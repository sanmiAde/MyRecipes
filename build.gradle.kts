plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.5.5"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("plugin.noarg") version "1.9.10"

}


group = "com.sanmiade"
version = "0.0.1-SNAPSHOT"
description = "MyRecipes"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.auth0:java-jwt:4.5.0")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.jetbrains.kotlin:kotlin-noarg")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.13")
    runtimeOnly("org.postgresql:postgresql")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
        exclude(group = "org.mockito", module = "mockito-core")
    }
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
    testImplementation("com.ninja-squad:springmockk:4.0.2")
    testImplementation("org.instancio:instancio-core:3.7.0")
    testImplementation("org.instancio:instancio-junit:3.7.0")
    testImplementation("io.mockk:mockk:1.13.7")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation(kotlin("test"))
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

noArg {
    annotation("jakarta.persistence.Entity")
}


allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}


tasks.withType<Test> {
    useJUnitPlatform()
}

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.junit.jupiter") {
            useVersion("5.10.0")
        }
        if (requested.group == "org.junit.platform") {
            useVersion("1.10.0")
        }
    }
}
