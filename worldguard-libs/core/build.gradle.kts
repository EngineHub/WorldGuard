import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

applyLibrariesConfiguration()

dependencies {
    "shade"("org.enginehub:squirrelid:${Versions.SQUIRRELID}")
    "shade"("org.khelekore:prtree:1.5.0")
}

tasks.named<ShadowJar>("jar") {
    dependencies {
        relocate("org.enginehub.squirrelid", "com.sk89q.worldguard.util.profile") {
            include(dependency("org.enginehub:squirrelid"))
        }

        include(dependency("org.khelekore:prtree"))
    }
}
