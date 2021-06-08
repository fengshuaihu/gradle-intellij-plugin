package org.jetbrains.intellij.tasks

import org.gradle.api.GradleException
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.internal.ConventionTask
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import org.gradle.util.VersionNumber
import org.jetbrains.intellij.IntelliJPluginConstants.VERSION_LATEST
import org.jetbrains.intellij.create
import org.jetbrains.intellij.debug
import org.jetbrains.intellij.extractArchive
import org.jetbrains.intellij.model.SpacePackagesMavenMetadata
import org.jetbrains.intellij.model.XmlExtractor
import java.net.URI
import java.net.URL
import javax.inject.Inject

@Suppress("UnstableApiUsage")
open class DownloadRobotServerPluginTask @Inject constructor(
    objectFactory: ObjectFactory,
    private val archiveOperations: ArchiveOperations,
    private val execOperations: ExecOperations,
    private val fileSystemOperations: FileSystemOperations,
) : ConventionTask() {

    companion object {
        const val ROBOT_SERVER_REPOSITORY = "https://cache-redirector.jetbrains.com/packages.jetbrains.team/maven/p/ij/intellij-dependencies"
        private const val ROBOT_SERVER_PLUGIN_METADATA_URL =
            "$ROBOT_SERVER_REPOSITORY/com/intellij/remoterobot/robot-server-plugin/maven-metadata.xml"
        const val OLD_ROBOT_SERVER_DEPENDENCY = "org.jetbrains.test:robot-server-plugin"
        const val NEW_ROBOT_SERVER_DEPENDENCY = "com.intellij.remoterobot:robot-server-plugin"

        fun resolveLatestVersion(): String {
            debug(message = "Resolving latest Robot Server Plugin version")
            val url = URL(ROBOT_SERVER_PLUGIN_METADATA_URL)
            return XmlExtractor<SpacePackagesMavenMetadata>().unmarshal(url.openStream()).versioning?.latest
                ?: throw GradleException("Cannot resolve the latest Robot Server Plugin version")
        }
    }

    @Input
    val version: Property<String> = objectFactory.property(String::class.java)

    @OutputDirectory
    val outputDir: DirectoryProperty = objectFactory.directoryProperty()

    @Transient
    private val dependencyHandler = project.dependencies

    @Transient
    private val repositoryHandler = project.repositories

    @Transient
    private val configurationContainer = project.configurations

    @Transient
    @Suppress("LeakingThis")
    private val context = this

    /**
     * Resolves Plugin Verifier version.
     * If set to {@link IntelliJPluginConstants#VERSION_LATEST}, there's request to {@link #VERIFIER_METADATA_URL}
     * performed for the latest available verifier version.
     *
     * @return Plugin Verifier version
     */
    private fun resolveVersion() = version.orNull?.takeIf { it != VERSION_LATEST } ?: resolveLatestVersion()

    @TaskAction
    fun downloadPlugin() {
        val resolvedVersion = resolveVersion()
        val (group, name) = getDependency(resolvedVersion).split(':')
        val dependency = dependencyHandler.create(
            group = group,
            name = name,
            version = resolvedVersion,
        )
        val repository = repositoryHandler.maven { it.url = URI.create(ROBOT_SERVER_REPOSITORY) }
        val target = outputDir.get().asFile

        try {
            val zipFile = configurationContainer.detachedConfiguration(dependency).singleFile
            extractArchive(zipFile, target, archiveOperations, execOperations, fileSystemOperations, context)
        } finally {
            repositoryHandler.remove(repository)
        }
    }

    private fun getDependency(version: String) = when {
        VersionNumber.parse(version) < VersionNumber.parse("0.11.0") -> OLD_ROBOT_SERVER_DEPENDENCY
        else -> NEW_ROBOT_SERVER_DEPENDENCY
    }
}
