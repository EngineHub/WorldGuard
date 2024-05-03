plugins {
    `kotlin-dsl`
    kotlin("jvm") version embeddedKotlinVersion
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(gradleApi())
    implementation("gradle.plugin.org.cadixdev.gradle:licenser:0.6.1")
    implementation("org.ajoberstar.grgit:grgit-gradle:5.2.2")
    implementation("com.github.johnrengelman:shadow:8.1.1")
    implementation("org.jfrog.buildinfo:build-info-extractor-gradle:5.2.0")
    constraints {
        val asmVersion = "[9.7,)"
        implementation("org.ow2.asm:asm:$asmVersion") {
            because("Need Java 21 support in shadow")
        }
        implementation("org.ow2.asm:asm-commons:$asmVersion") {
            because("Need Java 21 support in shadow")
        }
        implementation("org.vafer:jdependency:[2.10,)") {
            because("Need Java 21 support in shadow")
        }
    }
}
