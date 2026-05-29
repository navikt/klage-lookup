import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val testContainersVersion = "2.0.5"
val klageKodeverkVersion = "3.2.11"
val springMockkVersion = "5.0.1"
val mockkVersion = "1.14.9"
val tokenValidationVersion = "6.0.8"
val logstashVersion = "9.0"
val springDocVersion = "3.0.3"
val confluentVersion = "8.2.0"

plugins {
    val kotlinVersion = "2.3.21"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    id("org.springframework.boot") version "4.0.6"
    id("io.spring.dependency-management") version "1.1.7"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
    maven("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
    maven("https://packages.confluent.io/maven/")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-jetty")
    modules {
        module("org.springframework.boot:spring-boot-starter-tomcat") {
            replacedBy("org.springframework.boot:spring-boot-starter-jetty")
        }
    }
    implementation("org.eclipse.jetty.http2:jetty-http2-server")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("io.confluent:kafka-connect-avro-converter:${confluentVersion}") {
        exclude(group = "io.swagger.core.v3", module = "swagger-annotations")
    }
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springDocVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("net.logstash.logback:logstash-logback-encoder:${logstashVersion}")
    implementation("no.nav.klage:klage-kodeverk:$klageKodeverkVersion")
    implementation("no.nav.security:token-validation-spring:${tokenValidationVersion}")
    implementation("no.nav.security:token-client-spring:${tokenValidationVersion}")
    implementation("io.micrometer:micrometer-registry-prometheus")

    //Fix vulnerabilities, while waiting for fix in Spring Boot.
    implementation("org.apache.tomcat.embed:tomcat-embed-core:11.0.22")
    implementation("io.netty:netty-codec-http:4.2.14.Final")
    implementation("io.netty:netty-codec-http2:4.2.14.Final")
    implementation("io.netty:netty-codec-http3:4.2.14.Final")
    implementation("io.netty:netty-codec-dns:4.2.14.Final")
    implementation("io.netty:netty-codec-compression:4.2.14.Final")
    implementation("io.netty:netty-transport-native-epoll:4.2.14.Final")
    implementation("io.netty:netty-resolver-dns:4.2.14.Final")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage")
        exclude(group = "org.mockito")
    }
    testImplementation("org.springframework.boot:spring-boot-starter-data-redis-test") {
        exclude(group = "org.junit.vintage")
        exclude(group = "org.mockito")
    }
    testImplementation("org.testcontainers:testcontainers:${testContainersVersion}")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter:${testContainersVersion}")
    testImplementation("io.mockk:mockk:$mockkVersion")
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        freeCompilerArgs = listOf("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    this.archiveFileName.set("app.jar")
}