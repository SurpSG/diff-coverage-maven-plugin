package com.github.surpsg

import io.github.surpsg.deltacoverage.config.CoverageEntity
import java.io.File
import java.net.URL

class DiffSourceConfiguration(
    var url: URL? = null,
    var git: String? = null,
    var file: File? = null
) {
    override fun toString(): String {
        return "DiffSourceConfiguration(url=$url, git=$git, file=$file)"
    }
}

class ViolationsConfiguration {

    var failOnViolation: Boolean = false

    @Deprecated("This property will be removed in the next major release. Use 'rules' instead.")
    var minLines: Double = 0.0

    @Deprecated("This property will be removed in the next major release. Use 'rules' instead.")
    var minBranches: Double = 0.0

    @Deprecated("This property will be removed in the next major release. Use 'rules' instead.")
    var minInstructions: Double = 0.0

    var rules: List<Rule> = emptyList()

    var minCoverage: Double = MIN_COVERAGE_PROPERTY_DEFAULT

    override fun toString(): String {
        return "ViolationsConfiguration(failOnViolation=$failOnViolation, rules=$rules, minCoverage=$minCoverage)"
    }
}

class Rule {

    lateinit var coverageEntity: CoverageEntity

    var minCoverageRatio: Double = 0.0

    var entityCountThreshold: Int? = null

    override fun toString(): String {
        return "Rule(coverageEntity=$coverageEntity, minCoverageRatio=$minCoverageRatio, entityCountThreshold=$entityCountThreshold)"
    }
}

internal const val MIN_COVERAGE_PROPERTY_DEFAULT: Double = -1.0
