import buildlogic.getLibrary
import buildlogic.stringyLibs

plugins {
    id("eclipse")
    id("idea")
    id("checkstyle")
    id("buildlogic.common")
}

tasks
    .withType<JavaCompile>()
    .matching { it.name == "compileJava" || it.name == "compileTestJava" }
    .configureEach {
        val disabledLint = listOf(
            "processing", "path", "fallthrough", "serial", "overloads",
        )
        options.release.set(21)
        options.compilerArgs.addAll(listOf("-Xlint:all") + disabledLint.map { "-Xlint:-$it" })
        options.isDeprecation = true
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
        //options.compilerArgs.add("-Werror")
    }

configure<CheckstyleExtension> {
    configFile = rootProject.file("config/checkstyle/checkstyle.xml")
    toolVersion = "10.16.0"
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

dependencies {
    "compileOnly"(stringyLibs.getLibrary("jsr305"))
    "testImplementation"(platform(stringyLibs.getLibrary("junit-bom")))
    "testImplementation"(stringyLibs.getLibrary("junit-jupiter-api"))
    "testImplementation"(stringyLibs.getLibrary("junit-jupiter-params"))
    "testRuntimeOnly"(stringyLibs.getLibrary("junit-jupiter-engine"))
}

// Java 8 turns on doclint which we fail
tasks.withType<Javadoc>().configureEach {
    options.encoding = "UTF-8"
    (options as StandardJavadocDocletOptions).apply {
        //addBooleanOption("Werror", true)
        addBooleanOption("Xdoclint:all", true)
        addBooleanOption("Xdoclint:-missing", true)
        tags(
            "apiNote:a:API Note:",
            "implSpec:a:Implementation Requirements:",
            "implNote:a:Implementation Note:"
        )
    }
}

configure<JavaPluginExtension> {
    withJavadocJar()
    withSourcesJar()
}

tasks.named("check").configure {
    dependsOn("checkstyleMain", "checkstyleTest")
}
