package com.sgnat

import com.form.coverage.*
import com.form.coverage.report.ReportGenerator
import com.form.coverage.report.analyzable.AnalyzableReportFactory
import com.form.diff.CodeUpdateInfo
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

    override fun execute() {
        val analyzableReports = AnalyzableReportFactory().createCoverageAnalyzerFactory(
            setOf(
                DiffReport(
                    outputDirectory.resolve("diffCoverage").toPath(),
                    setOf(Report(ReportType.HTML, "")),
                    CodeUpdateInfo(emptyMap()),
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
}
