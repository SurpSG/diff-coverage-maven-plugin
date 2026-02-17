package io.github.surpsg

import io.github.surpsg.deltacoverage.config.ReportConfig
import io.github.surpsg.deltacoverage.config.ReportsConfig

internal fun buildDeltaCoverageReportConfig(
    reportDir: String,
    reports: List<Report>,
) = ReportsConfig {
    val enabledReports: Map<String, Boolean> = buildEnableReportMap(reports)
    baseReportDir = reportDir
    html = ReportConfig {
        outputFileName = "html"
        enabled = enabledReports.getOrDefault("html", true)
    }
    xml = ReportConfig {
        outputFileName = "report.xml"
        enabled = enabledReports.getOrDefault("xml", true)
    }
    console = ReportConfig {
        outputFileName = "console.txt"
        enabled = enabledReports.getOrDefault("console", true)
    }
    markdown = ReportConfig {
        outputFileName = "report.md"
        enabled = enabledReports.getOrDefault("markdown", true)
    }
    fullCoverageReport = false
}

private fun buildEnableReportMap(reports: List<Report>): Map<String, Boolean> = reports.associate {
    requireNotNull(it.type) to it.enabled
}
