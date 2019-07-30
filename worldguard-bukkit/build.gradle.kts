import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java-library")
    id("net.ltgt.apt-eclipse")
    id("net.ltgt.apt-idea")
}

applyPlatformAndCoreConfiguration()
applyShadowConfiguration()

repositories {
    maven {
        name = "spigot"
        url = uri("https://hub.spigotmc.org/nexus/content/groups/public")
    }
    maven {
        name = "bstats"
        url = uri("https://repo.codemc.org/repository/maven-public")
    }
}

dependencies {
    "compile"(project(":worldguard-core"))
    //"compile"(project(":worldguard-libs:bukkit"))
    "api"("org.bukkit:bukkit:1.14.2-R0.1-SNAPSHOT")
    "api"("com.sk89q.worldedit:worldedit-bukkit:7.0.1-SNAPSHOT") { isTransitive = false }
    "implementation"("com.sk89q:commandbook:2.3") { isTransitive = false }
    "implementation"("org.bstats:bstats-bukkit:1.5")
}

tasks.named<Copy>("processResources") {
    filesMatching("plugin.yml") {
        expand("internalVersion" to project.ext["internalVersion"])
    }
}

tasks.named<Jar>("jar") {
    manifest {
        attributes("Implementation-Version" to project.version)
    }
}

tasks.named<ShadowJar>("shadowJar") {
    dependencies {
        relocate("org.bstats", "com.sk89q.worldguard.bukkit.bstats") {
            include(dependency("org.bstats:bstats-bukkit:1.5"))
        }
    }
}

tasks.named("assemble").configure {
    dependsOn("shadowJar")
}