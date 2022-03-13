plugins {
    `java-library`
}

applyPlatformAndCoreConfiguration()

dependencies {
    "api"(project(":worldguard-libs:core"))
    "api"("com.sk89q.worldedit:worldedit-core:${Versions.WORLDEDIT}")
    "implementation"("org.flywaydb:flyway-core:3.0")
    "implementation"("org.yaml:snakeyaml:1.30")
    "implementation"("com.google.guava:guava:${Versions.GUAVA}")

    "api"("net.kyori:adventure-api:${Versions.ADVENTURE}")
    "api"("net.kyori:adventure-text-minimessage:${Versions.ADVENTURE}")
    "api"("net.kyori:adventure-text-serializer-legacy:${Versions.ADVENTURE}")

    "compileOnly"("com.google.code.findbugs:jsr305:${Versions.FINDBUGS}")
    "testImplementation"("org.hamcrest:hamcrest-library:1.2.1")
}

tasks.withType<JavaCompile>().configureEach {
    dependsOn(":worldguard-libs:build")
}