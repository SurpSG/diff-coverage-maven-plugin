package com.sgnat

import com.form.coverage.*
import com.form.coverage.report.ReportGenerator
import com.form.coverage.report.analyzable.AnalyzableReportFactory
import com.form.diff.CodeUpdateInfo
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import java.io.File


/**
 * Goal which touches a timestamp file.
 *
 * @goal touch
 *
 * @phase process-sources
 */

@Mojo(name = "diff-coverage", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
class DiffCoverageMojo : AbstractMojo() {
    /**
     * Location of the file.
     * @parameter expression="${project.build.directory}"
     * @required
     */
    @Parameter(defaultValue = "\${project.build.directory}", required = true, readonly = true)
    private lateinit var outputDirectory: File

    override fun execute() {
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs()
        }
        val analyzableReports = AnalyzableReportFactory().createCoverageAnalyzerFactory(
            setOf(
                DiffReport(
                    outputDirectory.resolve("diffCoverage").toPath(),
                    setOf(Report(ReportType.HTML, "")),
                    CodeUpdateInfo(emptyMap()),
                    Violation(true, listOf()
                    )
                ),
                FullReport(
                    outputDirectory.resolve("fullReport").toPath(),
                    setOf(Report(ReportType.HTML, ""))
                )
            )
        )
        ReportGenerator(
            outputDirectory.resolve("../"),
            setOf(outputDirectory.resolve("jacoco.exec")),
            setOf(outputDirectory.resolve("classes")),
            setOf(outputDirectory.resolve("../src/main/kotlin"))
        ).create(analyzableReports)
    }
}
