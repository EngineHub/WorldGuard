plugins {
    id("com.github.johnrengelman.shadow")
    id("buildlogic.core-and-platform")
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveClassifier.set("dist")
    dependencies {
        include(project(":worldguard-libs:core"))
        include(project(":worldguard-core"))

        relocate("org.flywaydb", "com.sk89q.worldguard.internal.flywaydb") {
            include(dependency("org.flywaydb:flyway-core"))
        }
        exclude("com.google.code.findbugs:jsr305")
    }
    exclude("GradleStart**")
    exclude(".cache")
    exclude("LICENSE*")
    exclude("META-INF/maven/**")
    minimize()
}
val javaComponent = components["java"] as AdhocComponentWithVariants
// I don't think we want this published (it's the shadow jar)
javaComponent.withVariantsFromConfiguration(configurations["shadowRuntimeElements"]) {
    skip()
}

afterEvaluate {
    tasks.named<Jar>("jar") {
        val version = project(":worldguard-core").version
        inputs.property("version", version)
        val attributes = mutableMapOf(
            "Implementation-Version" to version,
            "WorldGuard-Version" to version,
        )
        manifest.attributes(attributes)
    }
}
