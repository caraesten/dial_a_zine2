import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.10"
    kotlin("plugin.serialization") version "1.6.10"
    application
}

group = "me.carahurtle"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "15"
}

application {
    mainClass.set("com.dialazine.server.MainKt")
}

tasks.named<JavaExec>("run") {
    val port: Int by project
    val issueDirectory: String by project
    val indexFile: String by project

    systemProperty("port", port)
    systemProperty("issueDirectory", issueDirectory)
    systemProperty("indexFile", indexFile)
}
