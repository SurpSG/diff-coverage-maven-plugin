package com.github.surpsg

import com.form.coverage.config.DiffCoverageConfig
import com.form.coverage.config.DiffSourceConfig
import com.form.coverage.config.ReportConfig
import com.form.coverage.config.ReportsConfig
import com.form.coverage.config.ViolationRuleConfig
import com.form.coverage.report.ReportGenerator
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

    @Parameter(property = "jacoco.includes", required = false, defaultValue = "**")
    private var includes: List<String>? = emptyList();

    @Parameter(property = "jacoco.excludes", required = false, defaultValue = "")
    private var excludes: List<String>? = emptyList()

    @Parameter(defaultValue = "\${project.reporting.outputDirectory}")
    private lateinit var outputDirectory: File

    @Parameter(name = "diffSource", required = true)
    private lateinit var diffSource: DiffSourceConfiguration

    @Parameter(name = "violations")
    private var violations = ViolationsConfiguration()

    private val rootProjectDir: File
        get() = reactorProjects[0].basedir

    override fun execute() {
        val diffCoverageConfig: DiffCoverageConfig = buildDiffCoverageConfig().apply {
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

    private fun logPluginProperties(diffCoverageConfig: DiffCoverageConfig) {
        log.apply {
            debug("Root dir: $rootProjectDir")
            debug("Classes dirs: ${diffCoverageConfig.classFiles}")
            debug("Sources: ${diffCoverageConfig.sourceFiles}")
            debug("Exec files: ${diffCoverageConfig.execFiles}")
        }
    }

    private fun collectExecFiles(): Set<File> {
        return if (dataFileIncludes == null) {
            setOf(dataFile)
        } else {
            FileUtils.getFiles(rootProjectDir, dataFileIncludes, dataFileExcludes).toSet()
        }
    }

    private fun buildDiffCoverageConfig(): DiffCoverageConfig {
        return DiffCoverageConfig(
            reportName = rootProjectDir.name,
            diffSourceConfig = DiffSourceConfig(
                file = diffSource.file.asStringOrEmpty { absolutePath },
                url = diffSource.url.asStringOrEmpty { toString() },
                diffBase = diffSource.git ?: ""
            ),
            reportsConfig = ReportsConfig(
                baseReportDir = outputDirectory.absolutePath,
                html = ReportConfig(enabled = true, "html"),
                csv = ReportConfig(enabled = true, "diff-coverage.csv"),
                xml = ReportConfig(enabled = true, "diff-coverage.xml")
            ),
            violationRuleConfig = buildViolationRuleConfig(),
            execFiles = collectExecFiles(),
            reactorProjects
                .map {
                    val outputDir = File(it.build.outputDirectory)
                    val includePattern = includes?.joinToString(",")
                    val excludePattern = excludes?.joinToString(",")
                    FileUtils.getFiles(outputDir, includePattern, excludePattern)
                }
                .flatten()
                .toSet(),
            sourceFiles = reactorProjects.map { it.compileSourceRoots }.flatten().map { File(it) }.toSet()
        )
    }

    private fun buildViolationRuleConfig(): ViolationRuleConfig {
        val isMinCoverageSet: Boolean = violations.minCoverage != MIN_COVERAGE_PROPERTY_DEFAULT
        val configuredProperties: Set<Pair<String, Double>> = collectConfiguredCoveragePropertiesNames()

        if (isMinCoverageSet && configuredProperties.isNotEmpty()) {
            val conflictingProperties = configuredProperties.joinToString(separator = "\n") {
                "violations.${it.first} = ${it.second}"
            }
            throw IllegalArgumentException("""
                
                Simultaneous configuration of 'minCoverage' and any of [minCoverage, minBranches, minInstructions] is not allowed.
                violations.minCoverage = ${violations.minCoverage}
                $conflictingProperties
            """.trimIndent())
        }

        return if (isMinCoverageSet) {
            ViolationRuleConfig(
                minBranches = violations.minCoverage,
                minInstructions = violations.minCoverage,
                minLines = violations.minCoverage,
                failOnViolation = violations.failOnViolation
            )
        } else {
            ViolationRuleConfig(
                minBranches = violations.minBranches,
                minInstructions = violations.minInstructions,
                minLines = violations.minLines,
                failOnViolation = violations.failOnViolation
            )
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

    private fun <T> T?.asStringOrEmpty(toString: T.() -> String): String = if (this != null) {
        toString(this)
    } else {
        ""
    }

}
