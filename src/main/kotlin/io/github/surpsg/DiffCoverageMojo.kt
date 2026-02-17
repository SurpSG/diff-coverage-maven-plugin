package io.github.surpsg

import io.github.surpsg.deltacoverage.CoverageEngine
import io.github.surpsg.deltacoverage.config.CoverageEntity
import io.github.surpsg.deltacoverage.config.CoverageRulesConfig
import io.github.surpsg.deltacoverage.config.DeltaCoverageConfig
import io.github.surpsg.deltacoverage.config.DiffSourceConfig
import io.github.surpsg.deltacoverage.config.ViolationRule
import io.github.surpsg.deltacoverage.diff.DiffSource
import io.github.surpsg.deltacoverage.report.DeltaReportFacadeFactory
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
    private lateinit var reactorProjects: MutableList<MavenProject>

    @Parameter(property = "jacoco.dataFile", defaultValue = "\${project.build.directory}/jacoco.exec")
    private lateinit var dataFile: File

    @Parameter(property = "jacoco.dataFileIncludes", required = false)
    private var dataFileIncludes: String? = null

    @Parameter(property = "jacoco.dataFileExcludes", required = false)
    private var dataFileExcludes: String? = null

    @Parameter(defaultValue = ALL_FILES_PATTERN, required = false)
    private lateinit var includes: List<String>

    @Parameter(required = false)
    private var excludes: List<String> = emptyList()

    @Parameter(defaultValue = "\${project.reporting.outputDirectory}")
    private lateinit var outputDirectory: File

    @Parameter(name = "diffSource", required = true)
    private lateinit var diffSource: DiffSourceConfiguration

    @Parameter(name = "violations")
    private var violations = ViolationsConfiguration()

    @Parameter(name = "reports")
    private var reports: List<Report> = emptyList()

    private val rootProjectDir: File
        get() = reactorProjects[0].basedir

    override fun execute() {
        val diffCoverageConfig: DeltaCoverageConfig = buildDeltaCoverageConfig("aggregated").apply {
            logPluginProperties(this)
        }

        DeltaReportFacadeFactory
            .buildFacade(diffCoverageConfig.coverageEngine)
            .generateReports(diffCoverageConfig)
    }

    private fun logPluginProperties(diffCoverageConfig: DeltaCoverageConfig) {
        log.apply {
            debug("Root dir: $rootProjectDir")
            debug("Classes dirs: ${diffCoverageConfig.classFiles}")
            debug("Sources: ${diffCoverageConfig.sourceFiles}")
            debug("Exec files: ${diffCoverageConfig.binaryCoverageFiles}")
        }
    }

    private fun buildDeltaCoverageConfig(view: String) = DeltaCoverageConfig {
        coverageEngine = CoverageEngine.JACOCO
        viewName = view

        diffSource = DiffSource.buildDiffSource(
            rootProjectDir,
            DiffSourceConfig {
                file = this@DiffCoverageMojo.diffSource.file.asStringOrEmpty { absolutePath }
                url = this@DiffCoverageMojo.diffSource.url.asStringOrEmpty { toString() }
                diffBase = this@DiffCoverageMojo.diffSource.git ?: ""
            }
        )

        binaryCoverageFiles += collectExecFiles()
        sourceFiles += reactorProjects.flatMap { it.compileSourceRoots }.map { File(it) }.toSet()
        classFiles += collectClassesFiles().throwIfEmpty("Classes collection")

        coverageRulesConfig = buildCoverageRulesConfig()
        reportsConfig = buildDeltaCoverageReportConfig(outputDirectory.absolutePath, reports)
    }

    private fun buildCoverageRulesConfig(): CoverageRulesConfig {
        val isMinCoverageSet: Boolean = violations.minCoverage != MIN_COVERAGE_PROPERTY_DEFAULT
        val configuredProperties: Set<Pair<String, Double>> = collectConfiguredCoveragePropertiesNames()

        if (isMinCoverageSet && configuredProperties.isNotEmpty()) {
            val conflictingProperties = configuredProperties.joinToString(separator = "\n") {
                "violations.${it.first} = ${it.second}"
            }
            throw IllegalArgumentException(
                """
                
                Simultaneous configuration of 'minCoverage' and any of [minCoverage, minBranches, minInstructions] is not allowed.
                violations.minCoverage = ${violations.minCoverage}
                $conflictingProperties
            """.trimIndent()
            )
        }

        return CoverageRulesConfig {
            failOnViolation = violations.failOnViolation
            violationRules += if (isMinCoverageSet) {
                CoverageEntity.entries.map { entity ->
                    ViolationRule { coverageEntity = entity; minCoverageRatio = violations.minCoverage }
                }
            } else {
                buildCoverageRules()
            }
        }
    }

    private fun collectConfiguredCoveragePropertiesNames(): Set<Pair<String, Double>> {
        return sequenceOf(
            "minLines" to violations.minLines,
            "minBranches" to violations.minBranches,
            "minInstructions" to violations.minInstructions
        ).filter {
            it.second > 0.0
        }.toSet()
    }

    private fun buildCoverageRules(): Set<ViolationRule> = sequenceOf(
        CoverageEntity.LINE to violations.minLines,
        CoverageEntity.BRANCH to violations.minBranches,
        CoverageEntity.INSTRUCTION to violations.minInstructions
    )
        .filter { it.second > 0.0 }
        .map { (entity, value) ->
            ViolationRule { coverageEntity = entity; minCoverageRatio = value }
        }
        .toSet()

    private fun collectExecFiles(): Set<File> {
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
