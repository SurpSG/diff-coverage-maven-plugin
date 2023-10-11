package com.github.surpsg

import io.github.surpsg.deltacoverage.config.CoverageEntity
import io.github.surpsg.deltacoverage.config.CoverageRulesConfig
import io.github.surpsg.deltacoverage.config.ViolationRule

class CoverageRulesResolver {

    fun resolveFrom(violations: ViolationsConfiguration): CoverageRulesConfig {
        val configurationVariants: Set<ConfigurationVariant> = computeConfigurationVariant(violations)
        verifyViolationsConfiguration(configurationVariants)

        return CoverageRulesConfig {
            failOnViolation = violations.failOnViolation

            violationRules += configurationVariants.singleOrNull()
                ?.buildViolationRules(violations)
                ?: emptyList()
        }
    }

    private fun ConfigurationVariant.buildViolationRules(violations: ViolationsConfiguration): List<ViolationRule> =
        when (this) {
            ConfigurationVariant.LEGACY_PROPERTIES -> violations.buildViolationsFromLegacyProperties()
            ConfigurationVariant.MIN_COVERAGE_PROPERTY -> violations.buildViolationsFromMinCoverageProperty()
            ConfigurationVariant.RULES_PROPERTY -> violations.buildViolationsFromRulesCoverageProperty()
        }

    private fun ViolationsConfiguration.buildViolationsFromLegacyProperties(): List<ViolationRule> = listOf(
        CoverageEntity.INSTRUCTION.violationRule(minInstructions),
        CoverageEntity.BRANCH.violationRule(minBranches),
        CoverageEntity.LINE.violationRule(minLines),
    )

    private fun ViolationsConfiguration.buildViolationsFromMinCoverageProperty(): List<ViolationRule> =
        CoverageEntity.entries.map { entity ->
            entity.violationRule(minCoverage)
        }

    private fun ViolationsConfiguration.buildViolationsFromRulesCoverageProperty(): List<ViolationRule> =
        rules.map { rule ->
            ViolationRule {
                coverageEntity = rule.coverageEntity
                minCoverageRatio = rule.minCoverageRatio
                entityCountThreshold = rule.entityCountThreshold
            }
        }

    private fun computeConfigurationVariant(violations: ViolationsConfiguration): Set<ConfigurationVariant> =
        sequenceOf(
            ConfigurationVariant.RULES_PROPERTY.takeIf { violations.rules.isNotEmpty() },

            ConfigurationVariant.MIN_COVERAGE_PROPERTY.takeIf {
                violations.minCoverage != MIN_COVERAGE_PROPERTY_DEFAULT
            },

            ConfigurationVariant.LEGACY_PROPERTIES.takeIf {
                violations.minLines > 0.0 || violations.minBranches > 0.0 || violations.minInstructions > 0.0
            },
        ).mapNotNull { it }.toSet()

    private fun verifyViolationsConfiguration(configurationVariants: Set<ConfigurationVariant>) {
        require(configurationVariants.size <= 1) {
            val configuredProperties = configurationVariants.joinToString(", ") {
                it.propertyName
            }
            """
                
                Simultaneous violations configuration using:
                - ${ConfigurationVariant.RULES_PROPERTY.propertyName}
                - ${ConfigurationVariant.MIN_COVERAGE_PROPERTY.propertyName}
                - ${ConfigurationVariant.LEGACY_PROPERTIES.propertyName}
                is not allowed.
                But found configured properties: $configuredProperties.
            """.trimIndent()
        }
    }

    private fun CoverageEntity.violationRule(minValue: Double): ViolationRule = ViolationRule {
        coverageEntity = this@violationRule
        minCoverageRatio = minValue
    }

    private enum class ConfigurationVariant(
        val propertyName: String,
    ) {
        MIN_COVERAGE_PROPERTY("<${ViolationsConfiguration::minCoverage.name}>"),
        RULES_PROPERTY("<${ViolationsConfiguration::rules.name}>"),
        LEGACY_PROPERTIES(
            listOf(
                "<${ViolationsConfiguration::minLines.name}>",
                "<${ViolationsConfiguration::minBranches.name}>",
                "<${ViolationsConfiguration::minInstructions.name}>",
            ).joinToString(separator = ", ")
        ),
    }
}
