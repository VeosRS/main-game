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
