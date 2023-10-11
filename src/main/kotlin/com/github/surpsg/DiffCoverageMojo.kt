package com.github.surpsg

import io.github.surpsg.deltacoverage.config.DeltaCoverageConfig
import io.github.surpsg.deltacoverage.config.DiffSourceConfig
import io.github.surpsg.deltacoverage.config.ReportConfig
import io.github.surpsg.deltacoverage.config.ReportsConfig
import io.github.surpsg.deltacoverage.report.ReportGenerator
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject
import org.codehaus.plexus.util.FileUtils
import java.io.File

@Mojo(name = "diffCoverage", defaultPhase = LifecyclePhase.VERIFY)
class DiffCoverageMojo : AbstractMojo() {

    @Parameter(property = "reactorProjects", required = true, readonly = true)
    private var reactorProjects: MutableList<MavenProject> = mutableListOf()

    @Parameter(property = "jacoco.dataFile", defaultValue = "\${project.build.directory}/jacoco.exec")
    private lateinit var dataFile: File

    @Parameter(property = "jacoco.dataFileIncludes", required = false)
    private var dataFileIncludes: String? = null

    @Parameter(property = "jacoco.dataFileExcludes", required = false)
    private var dataFileExcludes: String? = null

    @Parameter(defaultValue = ALL_FILES_PATTERN, required = false)
    private var includes: List<String> = emptyList()

    @Parameter(required = false)
    private var excludes: List<String> = emptyList()

    @Parameter(defaultValue = "\${project.reporting.outputDirectory}")
    private lateinit var outputDirectory: File

    @Parameter(name = "diffSource", required = true)
    private lateinit var diffSource: DiffSourceConfiguration

    @Parameter(name = "violations")
    private var violations = ViolationsConfiguration()

    private val rootProjectDir: File
        get() = reactorProjects[0].basedir

    override fun execute() {
        val diffCoverageConfig: DeltaCoverageConfig = buildDeltaCoverageConfig().apply {
            logPluginProperties(this)
        }

        ReportGenerator(rootProjectDir, diffCoverageConfig).apply {
            val reportDir = File(diffCoverageConfig.reportsConfig.baseReportDir)
            reportDir.mkdirs()
            saveDiffToDir(reportDir).apply {
                log.info("diff content saved to '$absolutePath'")
            }

            create()
        }
    }

    private fun logPluginProperties(diffCoverageConfig: DeltaCoverageConfig) {
        log.apply {
            debug("Root dir: $rootProjectDir")
            debug("Classes dirs: ${diffCoverageConfig.classFiles}")
            debug("Sources: ${diffCoverageConfig.sourceFiles}")
            debug("Exec files: ${diffCoverageConfig.binaryCoverageFiles}")
        }
    }

    private fun buildDeltaCoverageConfig() = DeltaCoverageConfig {
        reportName = rootProjectDir.name

        binaryCoverageFiles += collectBinaryFiles()
        classFiles += collectClassesFiles().throwIfEmpty("Classes collection passed to Diff-Coverage")
        sourceFiles += reactorProjects.map { it.compileSourceRoots }.flatten().map { File(it) }.toSet()

        diffSourceConfig = DiffSourceConfig {
            file = diffSource.file.asStringOrEmpty { absolutePath }
            url = diffSource.url.asStringOrEmpty { toString() }
            diffBase = diffSource.git ?: ""
        }

        reportsConfig = ReportsConfig {
            baseReportDir = outputDirectory.absolutePath
            html = ReportConfig { enabled = true; outputFileName = "html" }
            csv = ReportConfig { enabled = true; outputFileName = "diff-coverage.csv" }
            xml = ReportConfig { enabled = true; outputFileName = "diff-coverage.xml" }
        }

        coverageRulesConfig = CoverageRulesResolver().resolveFrom(violations)
    }

    private fun collectBinaryFiles(): Set<File> {
        return if (dataFileIncludes == null) {
            setOf(dataFile)
        } else {
            FileUtils.getFiles(rootProjectDir, dataFileIncludes, dataFileExcludes).toSet()
        }
    }

    private fun collectClassesFiles(): Set<File> {
        val includePattern: String = includes.joinToString(",")
        val excludePattern: String = excludes.joinToString(",")
        return if (excludePattern.isEmpty() && includePattern == ALL_FILES_PATTERN) {
            reactorProjects.map { File(it.build.outputDirectory) }.toSet()
        } else {
            collectFilteredFiles(includePattern, excludePattern)
        }
    }

    private fun collectFilteredFiles(includePattern: String, excludePattern: String?): Set<File> {
        return reactorProjects.asSequence()
            .map { project -> File(project.build.outputDirectory) }
            .filter { outputDirectory -> outputDirectory.exists() }
            .flatMap { outputDirectory ->
                FileUtils.getFiles(
                    outputDirectory,
                    includePattern,
                    excludePattern
                )
            }.toSet()
    }

    private fun <T> T?.asStringOrEmpty(toString: T.() -> String): String = if (this != null) {
        toString(this)
    } else {
        ""
    }

    private fun <T> Set<T>.throwIfEmpty(collectionDescription: String): Set<T> {
        if (isEmpty()) {
            throw RuntimeException("$collectionDescription is empty")
        }
        return this
    }

    private companion object {
        const val ALL_FILES_PATTERN = "**"
    }

}
