package com.github.surpsg

import com.form.coverage.DiffReport
import com.form.coverage.Report
import com.form.coverage.ReportType
import com.form.coverage.Violation
import com.form.coverage.report.ReportGenerator
import com.form.coverage.report.analyzable.AnalyzableReport
import com.form.coverage.report.analyzable.AnalyzableReportFactory
import com.form.diff.CodeUpdateInfo
import com.form.diff.ModifiedLinesDiffParser
import com.github.surpsg.diffsource.getDiffSource
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject
import org.codehaus.plexus.util.FileUtils
import org.jacoco.core.analysis.ICoverageNode
import org.jacoco.report.check.Limit
import org.jacoco.report.check.Rule
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

    @Parameter(defaultValue = "\${project.reporting.outputDirectory}")
    private lateinit var outputDirectory: File

    @Parameter(name = "diffSource", required = true)
    private lateinit var diffSource: DiffSourceConfiguration

    @Parameter(name = "violations")
    private var violations = ViolationsConfiguration()

    private val rootProjectDir: File
        get() = reactorProjects[0].basedir

    override fun execute() {
        val execFiles: Set<File> = collectExecFiles()
        val classesSources: Set<File> = reactorProjects.map { File(it.build.outputDirectory) }.toSet()
        val sources: Set<File> = reactorProjects.map { it.compileSourceRoots }.flatten().map { File(it) }.toSet()

        logPluginProperties(execFiles, classesSources, sources)
        ReportGenerator(
            rootProjectDir,
            execFiles,
            classesSources,
            sources
        ).create(
            buildAnalyzableReports()
        )
    }

    private fun logPluginProperties(
        execFiles: Set<File>,
        classesSources: Set<File>,
        sources: Set<File>
    ) {
        log.apply {
            debug("Root dir: $rootProjectDir")
            debug("Classes dirs: $classesSources")
            debug("Sources: $sources")
            debug("Exec files: $execFiles")
        }
    }

    private fun collectExecFiles(): Set<File> = dataFileIncludes?.let {
        FileUtils.getFiles(rootProjectDir, it, dataFileExcludes).toSet()
    } ?: setOf(dataFile)

    private fun buildAnalyzableReports(): Set<AnalyzableReport> {
        return AnalyzableReportFactory().create(
            setOf(
                DiffReport(
                    outputDirectory.resolve(DIFF_COVERAGE_REPORT_FIR_NAME).toPath(),
                    setOf(
                        Report(ReportType.HTML, "html"),
                        Report(ReportType.XML, "diff-coverage.xml"),
                        Report(ReportType.CSV, "diff-coverage.csv")
                    ),
                    buildCodeUpdateInfo(),
                    Violation(
                        violations.failOnViolation,
                        listOf(buildRules())
                    )
                )
            )
        )
    }

    private fun buildCodeUpdateInfo(): CodeUpdateInfo {
        val diffSource = getDiffSource(rootProjectDir, diffSource).apply {
            log.debug("Starting to retrieve modified lines from $sourceDescription'")
            outputDirectory.resolve(DIFF_COVERAGE_REPORT_FIR_NAME).apply {
                mkdirs()
                val savedTo = saveDiffTo(this)
                log.info("diff content saved to '${savedTo.absolutePath}'")
            }
        }
        return ModifiedLinesDiffParser().collectModifiedLines(diffSource.pullDiff()).let {
            it.forEach { (file, rows) ->
                log.info("File $file has ${rows.size} modified lines")
                log.debug("File $file has modified lines $rows")
            }
            CodeUpdateInfo(it)
        }
    }

    private fun buildRules(): Rule {
        return sequenceOf(
            ICoverageNode.CounterEntity.INSTRUCTION to violations.minInstructions,
            ICoverageNode.CounterEntity.BRANCH to violations.minBranches,
            ICoverageNode.CounterEntity.LINE to violations.minLines
        ).filter {
            it.second > 0.0
        }.map {
            Limit().apply {
                setCounter(it.first.name)
                minimum = it.second.toString()
            }
        }.toList().let {
            Rule().apply {
                limits = it
            }
        }
    }

    companion object {
        const val DIFF_COVERAGE_REPORT_FIR_NAME = "diffCoverage"
    }
}
