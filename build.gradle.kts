import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper

plugins {
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.spring") version "1.6.10"
    id("org.springframework.boot") version "2.6.3"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("org.jmailen.kotlinter") version "3.8.0"
}

springBoot {
    mainClass.set("com.veosps.game.ApplicationKt")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":plugins"))
    findPlugins(project(":plugins")).forEach {
        implementation(it)
    }
}

fun findPlugins(pluginProject: ProjectDependency): List<Project> {
    val plugins = mutableListOf<Project>()
    pluginProject.dependencyProject.subprojects.forEach {
        if (it.buildFile.exists()) {
            plugins.add(it)
        }
    }
    return plugins
}

allprojects {
    group = "com.veosps.game"
    version = "1.0.0"

    apply {
        plugin("org.jetbrains.kotlin.jvm")
        plugin("io.spring.dependency-management")
        plugin("org.springframework.boot")
        plugin("org.jmailen.kotlinter")
    }

    kotlinter {
        disabledRules = arrayOf(
            "comment-spacing",
            "no-wildcard-imports",
            "final-newline",
            "indent",
            "filename",
            "import-ordering"
        )
    }

    repositories {
        mavenCentral()
    }

    dependencies {
        implementation(kotlin("stdlib"))

        implementation("org.rsmod:pathfinder:1.2.4")
        implementation("io.guthix:js5-filestore:0.5.0") {
            exclude("io.guthix", "jagex-bytebuf-extensions")
        }

        implementation("org.jetbrains.kotlin:kotlin-script-runtime:1.6.10")
        implementation("org.jetbrains.kotlin:kotlin-scripting-common:1.6.10")

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