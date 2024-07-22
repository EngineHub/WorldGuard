plugins {
    id("buildlogic.libs")
}

dependencies {
    "shade"(libs.squirrelid) {
        isTransitive = false
    }
    "shade"(libs.prtree)
}
