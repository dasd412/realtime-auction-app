import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    id("org.springframework.boot") version "3.2.3"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
    id("io.spring.dependency-management") version "1.1.4"
    id("nu.studer.jooq") version "8.2"
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

    // JOOQ
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.jooq:jooq:3.18.5")
    implementation("org.jooq:jooq-meta:3.18.5")
    implementation("org.jooq:jooq-codegen:3.18.5")

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

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.batch:spring-batch-test")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("org.testcontainers:testcontainers:1.19.3")
    testImplementation("org.testcontainers:mysql:1.19.3")
    testImplementation("org.testcontainers:junit-jupiter:1.19.3")

    // JOOQ 코드 생성을 위한 별도 의존성
    jooqGenerator("com.h2database:h2")
}

jooq {
    version.set("3.18.5") // JOOQ 버전 명시
    configurations {
        create("main") {
            jooqConfiguration.apply {
                jdbc.apply {
                    driver = "org.h2.Driver"
                    url = "jdbc:h2:mem:auction_db;DB_CLOSE_DELAY=-1"
                    user = "sa"
                    password = ""
                }
                generator.apply {
                    name = "org.jooq.codegen.KotlinGenerator"
                    database.apply {
                        name = "org.jooq.meta.h2.H2Database"
                        inputSchema = "PUBLIC"
                    }
                    generate.apply {
                        isDeprecated = false
                        isRecords = true
                        isPojos = true
                        isPojosEqualsAndHashCode = true
                    }
                    target.apply {
                        packageName = "com.auctionapp.jooq"
                        directory = "src/main/generated"
                    }
                    strategy.name = "org.jooq.codegen.DefaultGeneratorStrategy"
                }
            }
        }
    }
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

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// 데이터베이스 스키마가 생성된 후에 JOOQ 코드 생성이 실행되도록 설정
tasks.named("generateJooq") {
    // JPA가 먼저 스키마를 생성한 후에 JOOQ 코드 생성이 실행되어야 하지만,
    // 개발 초기에는 generateJooq를 직접 실행하지 않고 필요할 때 수동으로 실행하는 것이 좋습니다.
}

// 개발 초기에는 JOOQ 코드 생성에 의존하지 않도록 설정
tasks.named("compileKotlin") {
    // 데이터베이스가 존재하지 않으면 generateJooq가 실패하므로 dependsOn 제거
    // dependsOn("generateJooq")
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
