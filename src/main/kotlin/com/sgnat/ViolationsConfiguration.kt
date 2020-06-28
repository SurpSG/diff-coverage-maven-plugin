package com.sgnat

class ViolationsConfiguration(
    var failOnViolation: Boolean = false,
    var minLines: Double = 0.0,
    var minBranches: Double = 0.0,
    var minInstructions: Double = 0.0
) {
    override fun toString(): String {
        return "ViolationsConfiguration(" +
                "failOnViolation=$failOnViolation, " +
                "minLines=$minLines, " +
                "minBranches=$minBranches, " +
                "minInstructions=$minInstructions)"
    }
}
