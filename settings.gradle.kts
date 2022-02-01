import java.nio.file.Files
import java.nio.file.Path

rootProject.name = "veosps-server"

enableFeaturePreview("VERSION_CATALOGS")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include("all")
include("common")
include("plugins")
includePlugins(project(":plugins").projectDir.toPath())

fun includePlugins(pluginPath: java.nio.file.Path) {
    Files.walk(pluginPath).forEach {
        if (!Files.isDirectory(it)) {
            return@forEach
        }
        searchPlugin(pluginPath, it)
    }
}

fun searchPlugin(parent: Path, path: Path) {
    val hasBuildFile = Files.exists(path.resolve("build.gradle.kts"))
    if (!hasBuildFile) {
        return
    }
    val relativePath = parent.relativize(path)
    val pluginName = relativePath.toString().replace(File.separator, ":")

    include("plugins:$pluginName")
}
