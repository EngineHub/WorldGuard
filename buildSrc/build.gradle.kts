plugins {
    `kotlin-dsl`
    kotlin("jvm") version embeddedKotlinVersion
}

repositories {
    jcenter()
    gradlePluginPortal()
}

dependencies {
    implementation(gradleApi())
    implementation("gradle.plugin.net.minecrell:licenser:0.4.1")
    implementation("org.ajoberstar.grgit:grgit-gradle:3.1.1")
    implementation("com.github.jengelman.gradle.plugins:shadow:5.1.0")
    implementation("net.ltgt.apt-eclipse:net.ltgt.apt-eclipse.gradle.plugin:0.21")
    implementation("net.ltgt.apt-idea:net.ltgt.apt-idea.gradle.plugin:0.21")
    implementation("org.jfrog.buildinfo:build-info-extractor-gradle:4.9.7")
}