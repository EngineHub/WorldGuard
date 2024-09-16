pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

logger.lifecycle("""
*******************************************
 You are building WorldGuard!

 If you encounter trouble:
 1) Read COMPILING.md if you haven't yet
 2) Try running 'build' in a separate Gradle run
 3) Use gradlew and not gradle
 4) If you still need help, ask on Discord! https://discord.gg/enginehub

 Output files will be in [subproject]/build/libs
*******************************************
""")

rootProject.name = "worldguard"

includeBuild("build-logic")

include("worldguard-libs")
include("worldguard-libs:core")

listOf("bukkit", "core").forEach {
    include("worldguard-$it")
}
