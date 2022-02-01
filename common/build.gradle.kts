

tasks.create<JavaExec>("createRsa") {
    group = "veosps"
    classpath = sourceSets.main.get().runtimeClasspath
    description = "Creates RSA key pair"
    mainClass.set("com.veosps.game.tools.security.RsaGenerator")
    args = listOf(
        "2048",
        "16",
        rootProject.projectDir.toPath().resolve("./data/rsa/key.pem").toAbsolutePath().toString()
    )
}