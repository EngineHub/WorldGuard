plugins {
    `kotlin-dsl`
}

repositories {
    maven {
        name = "EngineHub"
        url = uri("https://repo.enginehub.org/libs-release/")
    }
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(gradleApi())
    implementation(libs.crankcase.java)
    implementation(libs.crankcase.javaLibrary)
    implementation(libs.crankcase.common)
    implementation(libs.crankcase.licensing)
    implementation(libs.crankcase.git)
    implementation(libs.crankcase.publishing)
    implementation(libs.shadow)
    implementation(libs.gson)

    constraints {
        val asmVersion = "[${libs.versions.minimumAsm.get()},)"
        implementation("org.ow2.asm:asm:$asmVersion") {
            because("Need Java 21 support in shadow")
        }
        implementation("org.ow2.asm:asm-commons:$asmVersion") {
            because("Need Java 21 support in shadow")
        }
        implementation("org.vafer:jdependency:[${libs.versions.minimumJdependency.get()},)") {
            because("Need Java 21 support in shadow")
        }
    }
}
