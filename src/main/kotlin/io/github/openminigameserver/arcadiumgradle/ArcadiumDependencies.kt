package io.github.openminigameserver.arcadiumgradle

data class Dependency(val group: String, val name: String, val version: String? = null) {
    fun format(version: String? = null): String {
        return "$group:$name${(version ?: this.version)?.let { ":$it" } ?: ""}"
    }
}

object ArcadiumDependencies {
    private val KOTLIN_MODULE_BASE = Dependency(
        "org.jetbrains.kotlin",
        ""
    )

    val KOTLIN_STDLIB = KOTLIN_MODULE_BASE.copy(name = "kotlin-stdlib")
    val KOTLIN_REFLECT = KOTLIN_MODULE_BASE.copy(name = "kotlin-reflect")

    val KOTLINX_DATETIME = Dependency("org.jetbrains.kotlinx", "kotlinx-datetime", "0.2.1")
    val KOTLINX_COROUTINES = Dependency("org.jetbrains.kotlinx", "kotlinx-coroutines-jdk8", "1.5.0")

    val ARCADIUM_API = Dependency(
        "io.github.nickacpt.deepslate",
        "deepslate-api",
        "1.17-R0.1-SNAPSHOT"
    )

    val ARCADIUM_ALL = ARCADIUM_API.copy(name = "deepslate")

    val NICKARCADE_CORE = Dependency(omsGroup, "NickArcadeBukkit", "1.0-SNAPSHOT")
    val NICKARCADE_PLUGIN = Dependency(omsGroup, "", "1.0-SNAPSHOT")
    val FLOODGATE_PLUGIN = Dependency("org.geysermc.floodgate", "api", "2.0-SNAPSHOT")
}