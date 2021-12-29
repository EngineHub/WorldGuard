import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.Project
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.register

fun Project.applyLibrariesConfiguration() {
    applyCommonConfiguration()
    apply(plugin = "java-base")
    apply(plugin = "maven-publish")
    apply(plugin = "com.github.johnrengelman.shadow")
    apply(plugin = "com.jfrog.artifactory")

    configurations {
        create("shade")
    }

    group = "${rootProject.group}.worldguard-libs"

    tasks.register<ShadowJar>("jar") {
        configurations = listOf(project.configurations["shade"])
        archiveClassifier.set("")

        dependencies {
            exclude(dependency("com.google.code.findbugs:jsr305:1.3.9"))
        }

        relocate("com.sk89q.squirrelid", "com.sk89q.worldguard.util.profile")
    }
    val altConfigFiles = { artifactType: String ->
        val deps = configurations["shade"].incoming.dependencies
                .filterIsInstance<ModuleDependency>()
                .map { it.copy() }
                .map { dependency ->
                    dependency.artifact {
                        name = dependency.name
                        type = artifactType
                        extension = "jar"
                        classifier = artifactType
                    }
                    dependency
                }

        files(configurations.detachedConfiguration(*deps.toTypedArray())
                .resolvedConfiguration.lenientConfiguration.artifacts
                .filter { it.classifier == artifactType }
                .map { zipTree(it.file) })
    }
    tasks.register<Jar>("sourcesJar") {
        from({
            altConfigFiles("sources")
        })
        val filePattern = Regex("(.*)com/sk89q/squirrelid((?:/|$).*)")
        val textPattern = Regex("com\\.sk89q\\.squirrelid")
        eachFile {
            filter {
                it.replaceFirst(textPattern, "com.sk89q.worldguard.util.profile")
            }
            path = path.replaceFirst(filePattern, "$1com/sk89q/worldguard/util/profile$2")
        }
        archiveClassifier.set("sources")
    }

    tasks.named("assemble").configure {
        dependsOn("jar", "sourcesJar")
    }

    artifacts {
        val jar = tasks.named("jar")
        add("default", jar) {
            builtBy(jar)
        }
        val sourcesJar = tasks.named("sourcesJar")
        add("archives", sourcesJar) {
            builtBy(sourcesJar)
        }
    }

    applyCommonArtifactoryConfig()
}