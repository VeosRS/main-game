import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper

plugins {
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.spring") version "1.6.10"
    id("org.springframework.boot") version "2.6.3" apply false
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
}

project(":all") {

    apply {
    }
}

allprojects {
    group = "com.veosps.game"
    version = "1.0.0"

    apply {
        plugin("org.jetbrains.kotlin.jvm")
        plugin("io.spring.dependency-management")
        plugin("org.springframework.boot")
    }

    repositories {
        mavenCentral()
    }

    dependencies {
        implementation(kotlin("stdlib"))

        implementation("io.guthix:jagex-store-5:0.4.0") {
            exclude("io.guthix", "jagex-bytebuf")
        }

        implementation("org.jetbrains.kotlin:kotlin-script-runtime:1.6.10")
        implementation("org.jetbrains.kotlin:kotlin-scripting-common:1.6.10")

        implementation("com.displee:rs-cache-library:6.8.1")
        implementation("io.netty:netty-all:4.1.72.Final")
        implementation("org.bouncycastle:bcprov-jdk16:1.46")
        implementation("com.google.guava:guava:31.0.1-jre")
        implementation("io.github.classgraph:classgraph:4.8.138")

        implementation("com.michael-bull.kotlin-inline-logger:kotlin-inline-logger-jvm:1.0.3")
        implementation("com.michael-bull.kotlin-result:kotlin-result-jvm:1.1.14")
        implementation("com.michael-bull.kotlin-retry:kotlin-retry:1.0.9")

        implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.1")
        implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.13.1")

        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
        implementation("org.jetbrains.kotlin:kotlin-script-runtime:1.6.10")
        implementation("org.jetbrains.kotlin:kotlin-scripting-common:1.6.10")

        implementation("org.springframework.boot:spring-boot-starter-web")
        implementation("org.springframework.boot:spring-boot-starter-data-jpa")
        implementation("org.springframework.boot:spring-boot-starter-jdbc")
        implementation("org.mindrot:jbcrypt:0.4")

        runtimeOnly("com.h2database:h2:2.1.210")
        implementation("com.zaxxer:HikariCP:5.0.1")

        testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
        testImplementation("io.mockk:mockk:1.12.2")
        testImplementation("org.amshove.kluent:kluent:1.68") {
            exclude("junit", "junit")
        }
    }

    tasks.withType<JavaCompile> {
        options.release.set(11)
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "11"
            freeCompilerArgs = listOf("-XXLanguage:+InlineClasses")
        }
    }
}