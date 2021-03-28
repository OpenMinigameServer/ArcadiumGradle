plugins {
    kotlin("jvm") version "1.4.21"
    `java-gradle-plugin`
    `maven-publish`
    id("com.gradle.plugin-publish") version "0.12.0"

}

group = "io.github.openminigameserver"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven(url = "https://plugins.gradle.org/m2/")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("kr.entree:spigradle:2.2.3")
    implementation("com.github.jengelman.gradle.plugins:shadow:6.1.0")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.21")
}

gradlePlugin {
    plugins {
        create("ArcadiumGradle") {
            id = "io.github.openminigameserver.arcadiumgradle"
            implementationClass = "io.github.openminigameserver.arcadiumgradle.NickArcadePlugin"
        }
    }
}