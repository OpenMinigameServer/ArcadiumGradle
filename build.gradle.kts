plugins {
    kotlin("jvm") version "1.5.0"
    `java-gradle-plugin`
    `maven-publish`
    id("com.gradle.plugin-publish") version "0.15.0"

}

group = "io.github.openminigameserver"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven(url = "https://plugins.gradle.org/m2/")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("kr.entree:spigradle:2.2.4")
    implementation("gradle.plugin.com.github.jengelman.gradle.plugins:shadow:7.0.0")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.0")
    implementation("gradle.plugin.org.zeroturnaround:gradle-jrebel-plugin:1.1.10")
}

gradlePlugin {
    plugins {
        create("ArcadiumGradle") {
            id = "io.github.openminigameserver.arcadiumgradle"
            implementationClass = "io.github.openminigameserver.arcadiumgradle.NickArcadePlugin"
        }
    }
}