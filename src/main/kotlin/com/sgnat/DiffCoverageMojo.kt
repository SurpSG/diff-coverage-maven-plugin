package com.sgnat

import com.form.coverage.*
import com.form.coverage.report.ReportGenerator
import com.form.coverage.report.analyzable.AnalyzableReportFactory
import com.form.diff.CodeUpdateInfo
import com.form.diff.ModifiedLinesDiffParser
import com.sgnat.diffsource.getDiffSource
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject
import java.io.File

@Mojo(name = "diff-coverage", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
class DiffCoverageMojo : AbstractMojo() {

    @Parameter(defaultValue = "\${project}", required = true, readonly = true)
    private lateinit var project: MavenProject

    @Parameter(property = "jacoco.dataFile", defaultValue = "\${project.build.directory}/jacoco.exec")
    private lateinit var dataFile: File

    @Parameter(defaultValue = "\${project.reporting.outputDirectory}")
    private lateinit var outputDirectory: File

    @Parameter(name = "diffSource", required = true)
    private lateinit var diffSource: DiffSourceConfiguration

    @Parameter(name = "violations")
    private lateinit var violations: ViolationsConfiguration

    override fun execute() {
        println(violations)
        val analyzableReports = AnalyzableReportFactory().createCoverageAnalyzerFactory(
            setOf(
                DiffReport(
                    outputDirectory.resolve(DIFF_COVERAGE_REPORT_FIR_NAME).toPath(),
                    setOf(Report(ReportType.HTML, "html")),
                    buildCodeUpdateInfo(),
                    Violation(true, listOf())
                )
            )
        )
        ReportGenerator(
            project.basedir,
            setOf(dataFile),
            File(project.build.outputDirectory).walk().toSet(),
            project.compileSourceRoots.asSequence().map { File(it) }.toSet()
        ).create(analyzableReports)
    }

    private fun buildCodeUpdateInfo(): CodeUpdateInfo {
        val diffSource = getDiffSource(diffSource).apply {
            log.debug("Starting to retrieve modified lines from $sourceDescription'")
            saveDiffTo(outputDirectory.resolve(DIFF_COVERAGE_REPORT_FIR_NAME)).apply {
                log.info("diff content saved to '$absolutePath'")
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

    companion object {
        const val DIFF_COVERAGE_REPORT_FIR_NAME = "diffCoverage"
    }
}
