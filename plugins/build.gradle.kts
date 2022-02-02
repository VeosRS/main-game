val rootPluginDir = projectDir
val rootPluginBuildDir = buildDir

val appDir = rootProject.projectDir
val pluginConfigDir = appDir.resolve("data").resolve("plugins").resolve("resources")

allprojects {

    tasks.bootJarMainClassName {
        enabled = false
    }

    tasks.bootJar {
        enabled = false
    }

    dependencies {
        implementation(project(":common"))
    }
}

subprojects {
    val relative = projectDir.relativeTo(rootPluginDir)
    buildDir = rootPluginBuildDir.resolve(relative)
    group = "com.veosps.game.plugins"

    tasks.register("install") {
        group = "veosps-plugins"
        copyResources(project)
    }
}

tasks.register("install-plugins") {
    group = "veosps-plugins"

    subprojects.forEach { project ->
        copyResources(project)
    }
}

tasks.register("install-plugins-fresh") {
    group = "veosps-plugins"

    file(pluginConfigDir).deleteRecursively()
    subprojects.forEach { project ->
        copyResources(project)
    }
}

fun copyResources(project: Project) {
    val relativePluginDir = project.projectDir.relativeTo(rootPluginDir)
    val pluginResourceFiles = project.sourceSets.main.get().resources.asFileTree
    if (pluginResourceFiles.isEmpty) return
    val configDirectory = pluginConfigDir.resolve(relativePluginDir)
    pluginResourceFiles.forEach { file ->
        val existingFile = configDirectory.resolve(file.name)
        if (existingFile.exists()) {
            /* do not overwrite existing config files */
            return@forEach
        }
        copy {
            from(file)
            into(configDirectory)
        }
    }
}
