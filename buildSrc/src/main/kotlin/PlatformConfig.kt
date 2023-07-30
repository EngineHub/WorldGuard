import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.Project
import org.gradle.api.component.AdhocComponentWithVariants
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register

fun Project.applyPlatformAndCoreConfiguration(javaRelease: Int = 17) {
    applyCommonConfiguration()
    apply(plugin = "java")
    apply(plugin = "maven-publish")
    apply(plugin = "com.jfrog.artifactory")
    applyCommonJavaConfiguration(
        sourcesJar = true,
        javaRelease = javaRelease,
        banSlf4j = false
    )

    ext["internalVersion"] = "$version+${rootProject.ext["gitCommitHash"]}"

    configure<PublishingExtension> {
        publications {
            register<MavenPublication>("maven") {
                from(components["java"])
                versionMapping {
                    usage("java-api") {
                        fromResolutionOf("runtimeClasspath")
                    }
                    usage("java-runtime") {
                        fromResolutionResult()
                    }
                }
            }
        }
    }

    applyCommonArtifactoryConfig()
}

fun Project.applyShadowConfiguration() {
    apply(plugin = "com.github.johnrengelman.shadow")
    tasks.named<ShadowJar>("shadowJar") {
        archiveClassifier.set("dist")
        dependencies {
            include(project(":worldguard-libs:core"))
            //include(project(":worldguard-libs:${project.name.replace("worldguard-", "")}"))
            include(project(":worldguard-core"))

            relocate("org.flywaydb", "com.sk89q.worldguard.internal.flywaydb") {
                include(dependency("org.flywaydb:flyway-core:3.0"))
            }
            exclude("com.google.code.findbugs:jsr305")
        }
        exclude("GradleStart**")
        exclude(".cache")
        exclude("LICENSE*")
        exclude("META-INF/maven/**")
    }
    val javaComponent = components["java"] as AdhocComponentWithVariants
    // I don't think we want this published (it's the shadow jar)
    javaComponent.withVariantsFromConfiguration(configurations["shadowRuntimeElements"]) {
        skip()
    }
}
