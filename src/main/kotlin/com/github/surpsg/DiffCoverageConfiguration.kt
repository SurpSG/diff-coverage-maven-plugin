package com.github.surpsg

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

class ViolationsConfiguration(
    var failOnViolation: Boolean = false,
    var minLines: Double = 0.0,
    var minBranches: Double = 0.0,
    var minInstructions: Double = 0.0,

    var minCoverage: Double = MIN_COVERAGE_PROPERTY_DEFAULT
)

internal const val MIN_COVERAGE_PROPERTY_DEFAULT: Double = -1.0;
