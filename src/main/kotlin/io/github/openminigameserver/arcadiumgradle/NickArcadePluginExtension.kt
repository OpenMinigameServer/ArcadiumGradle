package io.github.openminigameserver.arcadiumgradle

open class NickArcadePluginExtension {
    var name: String = ""
    var arcadiumVersion: String = "1.16.5-R0.1-SNAPSHOT"
    var disableChangingModulesCache: Boolean = false
    var isCoreProject: Boolean = false
    var publishShaded: Boolean = false
    var depends = mutableListOf<String>()

    fun depends(vararg depend: String) {
        depends = depend.toMutableList()
    }
}
