package io.github.openminigameserver.arcadiumgradle

import com.github.jengelman.gradle.plugins.shadow.ShadowExtension
import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import io.github.openminigameserver.arcadiumgradle.ArcadiumDependencies.FLOODGATE_PLUGIN
import kr.entree.spigradle.module.spigot.SpigotExtension
import kr.entree.spigradle.module.spigot.SpigotPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.jvm.tasks.Jar
import org.gradle.plugins.ide.eclipse.internal.AfterEvaluateHelper
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File

open class NickArcadePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply("org.jetbrains.kotlin.jvm")
        project.pluginManager.apply(MavenPublishPlugin::class.java)
        project.pluginManager.apply(ShadowPlugin::class.java)
        project.pluginManager.apply(SpigotPlugin::class.java)
        project.pluginManager.apply("org.zeroturnaround.gradle.jrebel")

        val extension = project.extensions.create("nickarcade", NickArcadePluginExtension::class.java)

        AfterEvaluateHelper.afterEvaluateOrExecute(project) {
            extension.apply {
                if (isCoreProject) name = coreProjectName
            }

            dumpExtension(extension)
            setProjectDefaults(it)
            addDefaultRepositories(it)
            setupKotlinDefaults(it)
            addShadowJarIntoBuild(extension, it)
            addShadowJarPublishing(extension, it)
            disableChangingModulesCache(extension, it)
            applyDefaultDependencies(extension, it)
            configureSpigot(extension, it)
        }
    }


    private fun dumpExtension(extension: NickArcadePluginExtension) {
        with(extension) {
            println("Project name: $name")
            println("Arcadium server version: $arcadiumVersion")
            println("Maven publish shaded: $publishShaded")
        }
    }

    private fun addShadowJarPublishing(extension: NickArcadePluginExtension, project: Project) {
        with(project) {
            tasks.getByName("shadowJar").finalizedBy("publishToMavenLocal")
        }

        val shadow = project.extensions.getByType(ShadowExtension::class.java)
        configureShadow(extension, project)
        val publishingExtension = project.extensions.getByType(PublishingExtension::class.java)
        publishingExtension.publications { publication ->
            with(publication) {
                create("shadow", MavenPublication::class.java) { mavenPublication ->
                    if (extension.publishShaded) {
                        shadow.component(mavenPublication)
                    } else {
                        mavenPublication.from(project.components.getByName("java"))
                    }
                }
            }
        }
    }

    private fun configureShadow(extension: NickArcadePluginExtension, project: Project) {
        val jar = project.tasks.getByName("jar") as Jar
        jar.archiveClassifier.set("impl-only")

        val shadow = project.tasks.getByName("shadowJar") as ShadowJar
        val classifierName: String? = null
        shadow.apply {
            exclude("module-info.class")
            if (!extension.isCoreProject)
                exclude("kotlin/**")
        }
        shadow.archiveClassifier.set(classifierName)
        shadow.destinationDirectory.set(File("""D:\NickArcadeWork\plugins\"""))
    }

    private fun applyDefaultDependencies(extension: NickArcadePluginExtension, project: Project) {
        applyArcadiumDependency(extension, project)
        if (extension.isCoreProject) {
            applyExtraPluginDependency(project)
            applyKotlinDependency(project)
        }
        if (!extension.isCoreProject) {
            applyCoreDependency(project)
        }

        //Apply plugin dependency to project
        extension.depends.forEach {
            applyArcadePluginDependency(project, it)
        }
    }

    private fun applyExtraPluginDependency(project: Project) {
        project.dependencies.addDependency(FLOODGATE_PLUGIN)
    }

    private fun configureSpigot(extension: NickArcadePluginExtension, project: Project) {
        val spigot = project.extensions.getByType(SpigotExtension::class.java)
        if (!extension.isCoreProject) {
            spigot.depends += nickArcadePlugin(coreProjectName)
        }
        configureSpigotExtension(project, extension, spigot)
    }

    private fun applyArcadePluginDependency(project: Project, plugin: String) {
        val dependency = project.dependencies.addDependency(
            ArcadiumDependencies.NICKARCADE_PLUGIN.copy(name = nickArcadePlugin(plugin)),
            JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME
        ) as ExternalModuleDependency

        dependency.isChanging = true
    }

    private fun applyCoreDependency(project: Project) {
        val dependency = project.dependencies.addDependency(
            ArcadiumDependencies.NICKARCADE_CORE,
            JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME
        ) as ExternalModuleDependency

        dependency.isChanging = true
    }

    private fun applyKotlinDependency(project: Project) {
        project.dependencies.apply {
            addDependency(ArcadiumDependencies.KOTLIN_STDLIB)
            addDependency(ArcadiumDependencies.KOTLIN_REFLECT)

            addDependency(ArcadiumDependencies.KOTLINX_COROUTINES)
            addDependency(ArcadiumDependencies.KOTLINX_DATETIME)
        }
    }

    private fun DependencyHandler.addDependency(
        dependency: Dependency,
        configuration: String = JavaPlugin.API_CONFIGURATION_NAME
    ): org.gradle.api.artifacts.Dependency? {
        return add(
            configuration,
            dependency.format()
        )
    }

    private fun setProjectDefaults(project: Project) {
        with(project) {
            group = omsGroup
            version = "1.0-SNAPSHOT"
        }
    }

    private fun configureSpigotExtension(
        project: Project,
        extension: NickArcadePluginExtension,
        spigotExtension: SpigotExtension
    ) {
        with(spigotExtension) {
            name = nickArcadePlugin(extension.name)
            authors("NickAc")
            apiVersion = "1.16"
            if (!extension.isCoreProject) {
                depends = depends + nickArcadePlugin(coreProjectName)
            }

            //Automatically add the depends to the spigot config
            depends = depends + extension.depends.map { nickArcadePlugin(it) }

            if (extension.isCoreProject) {
                addPluginExternalDepends()
            }
            debug.eula = true
            version = project.version.toString()
        }
    }

    private fun SpigotExtension.addPluginExternalDepends() {
        depends = depends + "floodgate"
    }

    private fun setupKotlinDefaults(project: Project) {
        with(project) {
            extensions.getByType(JavaPluginExtension::class.java).withSourcesJar()
            val compileKotlin = tasks.getByName("compileKotlin") as KotlinCompile
            compileKotlin.kotlinOptions {
                jvmTarget = "11"
                freeCompilerArgs =
                    freeCompilerArgs + "-Xjvm-default=enable" + "-Xopt-in=kotlin.RequiresOptIn" + "-Xopt-in=kotlin.time.ExperimentalTime" + "-Xopt-in=kotlin.contracts.ExperimentalContracts" + "-Xinline-classes"
            }
        }
    }

    val repos = arrayOf(
        "https://kotlin.bintray.com/kotlinx/",
        "https://repo.incendo.org/content/repositories/snapshots",
        "https://repo.rapture.pw/repository/maven-snapshots/",
        "https://repo.spongepowered.org/maven",
        "https://repo.glaremasters.me/repository/concuncan/",
        "https://repo.opencollab.dev/maven-snapshots/"
    )

    private fun addDefaultRepositories(project: Project) {
        with(project.repositories) {
            mavenLocal()
            repos.forEach { repo ->
                this.maven {
                    it.setUrl(repo)
                }
            }
        }
    }

    private fun addShadowJarIntoBuild(extension: NickArcadePluginExtension, project: Project) {
        project.tasks.getByName("assemble").dependsOn("shadowJar")
        if (!extension.publishShaded) {
            project.tasks.getByName("jar").dependsOn("shadowJar")
        }
        project.tasks.getByName("build").dependsOn("publishToMavenLocal")
    }

    private fun applyArcadiumDependency(extension: NickArcadePluginExtension, project: Project) {
        val dependency = project.dependencies.add(
            JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME,
            ArcadiumDependencies.ARCADIUM_API.format(extension.arcadiumVersion)
        ) as ExternalModuleDependency

        dependency.isChanging = true
    }

    private fun disableChangingModulesCache(
        extension: NickArcadePluginExtension,
        project: Project
    ) {
        if (extension.disableChangingModulesCache) {
            project.configurations.all {
                it.resolutionStrategy.cacheChangingModulesFor(0, "seconds")
            }
        }
    }
}