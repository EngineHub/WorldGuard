import buildlogic.getLibrary
import buildlogic.stringyLibs

plugins {
    id("eclipse")
    id("idea")
    id("checkstyle")
    id("org.enginehub.crankcase.java")
    id("buildlogic.common")
}

crankcaseJava {
    javaRelease = 25
    disabledLints = listOf("processing", "path", "fallthrough", "serial", "overloads")
}

tasks.withType<JavaCompile>().configureEach {
    // TODO Remove this when we're able
    options.compilerArgs.remove("-Werror")
}

configure<CheckstyleExtension> {
    configFile = rootProject.file("config/checkstyle/checkstyle.xml")
    toolVersion = "10.16.0"
}

dependencies {
    "compileOnly"(stringyLibs.getLibrary("jsr305"))
}
