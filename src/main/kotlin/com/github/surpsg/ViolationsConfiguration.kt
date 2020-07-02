package com.github.surpsg

class ViolationsConfiguration(
    var failOnViolation: Boolean = false,
    var minLines: Double = 0.0,
    var minBranches: Double = 0.0,
    var minInstructions: Double = 0.0
)
