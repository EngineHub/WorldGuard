import org.cadixdev.gradle.licenser.LicenseExtension

plugins {
    `java-library`
    id("buildlogic.core-and-platform")
}

dependencies {
    constraints {
        "implementation"(libs.snakeyaml) {
            because("Bukkit provides SnakeYaml")
        }
    }

    "api"(project(":worldguard-libs:core"))
    "api"(libs.worldedit.core)
    "implementation"(libs.flyway.core)
    "implementation"(libs.snakeyaml)
    "implementation"(libs.guava)
    "compileOnlyApi"(libs.jsr305)
    "implementation"(libs.gson)

    "compileOnly"(libs.worldedit.libs.ap)
    "annotationProcessor"(libs.worldedit.libs.ap)
    // ensure this is on the classpath for the AP
    "annotationProcessor"(libs.guava)

    "compileOnly"(libs.autoService) {
        because("Needed to resolve annotations in Piston")
    }

    "testImplementation"(libs.hamcrest.library)
}

tasks.compileJava {
    dependsOn(":worldguard-libs:build")
    options.compilerArgs.add("-Aarg.name.key.prefix=")
}

configure<LicenseExtension> {
    exclude {
        it.file.startsWith(project.layout.buildDirectory.get().asFile)
    }
}

configure<PublishingExtension> {
    publications.named<MavenPublication>("maven") {
        artifactId = the<BasePluginExtension>().archivesName.get()
        from(components["java"])
    }
}
