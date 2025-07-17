import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    id("org.springframework.boot") version "3.2.3"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
    id("io.spring.dependency-management") version "1.1.4"
    id("nu.studer.jooq") version "8.2"
    id("jacoco")
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.spring") version "1.9.23"
    kotlin("plugin.jpa") version "1.9.23"
    kotlin("kapt") version "1.9.23"
}

group = "com.auctionapp"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-batch")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    implementation("org.springframework.boot:spring-boot-starter-quartz") // Scheduler

    // Kotlin
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

    // Database
    implementation("com.mysql:mysql-connector-j")
    implementation("com.h2database:h2")

    // Redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.redisson:redisson-spring-boot-starter:3.25.2")

    // Logging
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    // 기존 slf4j-simple 의존성을 제외
    implementation("org.slf4j:slf4j-api")

    // slf4j-simple 의존성이 다른 의존성에 포함되어 있다면 제외 처리
    configurations.all {
        exclude(group = "org.slf4j", module = "slf4j-simple")
    }

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.batch:spring-batch-test")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("org.testcontainers:testcontainers:1.19.3")
    testImplementation("org.testcontainers:mysql:1.19.3")
    testImplementation("org.testcontainers:junit-jupiter:1.19.3")
    testImplementation("it.ozimov:embedded-redis:0.7.3")

    // Specification
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.0.2")
}

jacoco {
    toolVersion = "0.8.10" // 최신 버전 사용
}

ktlint {
    verbose.set(true)
    outputToConsole.set(true)
    filter {
        exclude("**/generated/**")
        include("**/kotlin/**")
    }

    disabledRules.set(
        setOf(
            "no-wildcard-imports",
        ),
    )
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    dependsOn(tasks.test) // 테스트 실행 후 보고서 생성
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.7".toBigDecimal() // 전체 프로젝트 70% 최소 커버리지 요구
            }
        }
    }
}

// 테스트와 커버리지 검증을 함께 실행하는 태스크
tasks.register("testWithCoverage") {
    dependsOn(tasks.test, tasks.jacocoTestReport, tasks.jacocoTestCoverageVerification)
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

sourceSets {
    main {
        java {
            srcDir("src/main/generated")
        }
    }
}

tasks.register("cleanGenerated") {
    doLast {
        file("src/main/generated").deleteRecursively()
    }
}

tasks.named("clean") {
    dependsOn("cleanGenerated")
}
