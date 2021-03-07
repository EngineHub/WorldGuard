import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.internal.HasConvention

plugins {
    id("java-library")
    id("net.ltgt.apt-eclipse")
    id("net.ltgt.apt-idea")
}

applyPlatformAndCoreConfiguration()
applyShadowConfiguration()

repositories {
    maven {
        name = "paper"
        url = uri("https://papermc.io/repo/repository/maven-public/")
    }
    maven {
        name = "bstats"
        url = uri("https://repo.codemc.org/repository/maven-public")
    }
    maven {
        name = "aikar-timings"
        url = uri("http://repo.aikar.co/nexus/content/groups/aikar/")
    }
}

configurations {
    compileClasspath.extendsFrom(create("shade"))
}

dependencies {
    "implementation"(project(":worldguard-core"))
    //"compile"(project(":worldguard-libs:bukkit"))
    "api"("com.destroystokyo.paper:paper-api:1.16.2-R0.1-SNAPSHOT")
    "shade"("io.papermc:paperlib:1.0.4")
    "api"("com.sk89q.worldedit:worldedit-bukkit:${Versions.WORLDEDIT}") { isTransitive = false }
    "implementation"("com.google.guava:guava:${Versions.GUAVA}")
    "implementation"("com.sk89q:commandbook:2.3") { isTransitive = false }
    "shade"("org.bstats:bstats-bukkit:1.7")
    "shade"("co.aikar:minecraft-timings:1.0.4")
}

tasks.named<Upload>("install") {
    (repositories as HasConvention).convention.getPlugin<MavenRepositoryHandlerConvention>().mavenInstaller {
        pom.whenConfigured {
            dependencies.firstOrNull { dep ->
                dep!!.withGroovyBuilder {
                    getProperty("groupId") == "com.destroystokyo.paper" && getProperty("artifactId") == "paper-api"
                }
            }?.withGroovyBuilder {
                setProperty("groupId", "org.spigotmc")
                setProperty("artifactId", "spigot-api")
            }
        }
    }
}

tasks.named<Copy>("processResources") {
    val internalVersion = project.ext["internalVersion"]
    println("internalVersion=$internalVersion")
    inputs.property("internalVersion", internalVersion)
    filesMatching("plugin.yml") {
        expand("internalVersion" to internalVersion)
    }
}

tasks.named<Jar>("jar") {
    val projectVersion = project.properties["gitVersion"]
    version = projectVersion.toString()
    println("projectVersion=$projectVersion")
    println("version=$version")
    inputs.property("projectVersion", projectVersion)
    manifest {
        attributes("Implementation-Version" to projectVersion)
    }
}

tasks.named<ShadowJar>("shadowJar") {
    configurations = listOf(project.configurations["shade"], project.configurations["runtimeClasspath"])

    dependencies {
        relocate("org.bstats", "com.sk89q.worldguard.bukkit.bstats") {
            include(dependency("org.bstats:bstats-bukkit"))
        }
        relocate("io.papermc.lib", "com.sk89q.worldguard.bukkit.paperlib") {
            include(dependency("io.papermc:paperlib"))
        }
        relocate("co.aikar.timings.lib", "com.sk89q.worldguard.bukkit.timingslib") {
            include(dependency("co.aikar:minecraft-timings"))
        }
    }
}

tasks.named("assemble").configure {
    dependsOn("shadowJar")
}
