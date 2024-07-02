import org.ajoberstar.grgit.Grgit

plugins {
    alias(libs.plugins.codecov)
    id("buildlogic.common")
    id("buildlogic.artifactory-root")
}

if (!project.hasProperty("gitCommitHash")) {
    apply(plugin = "org.ajoberstar.grgit")
    ext["gitCommitHash"] = try {
        extensions.getByName<Grgit>("grgit").head()?.abbreviatedId
    } catch (e: Exception) {
        logger.warn("Error getting commit hash", e)

        "no.git.id"
    }
}
