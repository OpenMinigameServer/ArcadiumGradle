package io.github.openminigameserver.arcadiumgradle

import org.gradle.api.artifacts.dsl.DependencyHandler

fun DependencyHandler.arcadiumAll(version: String? = null) = ArcadiumDependencies.ARCADIUM_ALL.format(version)

fun DependencyHandler.arcadium(version: String? = null) = ArcadiumDependencies.ARCADIUM_API.format(version)

fun DependencyHandler.oms(name: String, version: String? = "1.0-SNAPSHOT") = "io.github.openminigameserver:$name:$version"