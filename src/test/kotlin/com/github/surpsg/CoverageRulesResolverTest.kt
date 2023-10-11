package com.github.surpsg

import io.github.surpsg.deltacoverage.config.CoverageEntity
import io.github.surpsg.deltacoverage.config.CoverageRulesConfig
import io.github.surpsg.deltacoverage.config.ViolationRule
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import io.kotest.matchers.maps.shouldContainExactly
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CoverageRulesResolverTest {

    private val coverageRulesResolver = CoverageRulesResolver()

    @Test
    fun `should build empty coverage rules if violations configuration is empty`() {
        // GIVEN
        val violationsConfiguration = ViolationsConfiguration()

        // WHEN
        val actualCoverageRules: CoverageRulesConfig = coverageRulesResolver.resolveFrom(violationsConfiguration)

        // THEN
        actualCoverageRules shouldBeEqualToComparingFields CoverageRulesConfig {
            failOnViolation = false
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `should build coverage rules with fail on violation value if configuration has true property`(
        failOnViolationConfiguration: Boolean
    ) {
        // GIVEN
        val violationsConfiguration = ViolationsConfiguration().apply {
            failOnViolation = failOnViolationConfiguration
        }

        // WHEN
        val actualCoverageRules: CoverageRulesConfig = coverageRulesResolver.resolveFrom(violationsConfiguration)

        // THEN
        actualCoverageRules shouldBeEqualToComparingFields CoverageRulesConfig {
            failOnViolation = failOnViolationConfiguration
        }
    }

    @ParameterizedTest
    @MethodSource("multiple variants configuration test parameters")
    fun `should throw if configured multiple configuration variants`(
        // GIVEN
        violationsConfiguration: ViolationsConfiguration,
    ) {
        // WHEN // THEN
        shouldThrow<IllegalArgumentException> {
            coverageRulesResolver.resolveFrom(violationsConfiguration)
        }
    }

    private fun `multiple variants configuration test parameters`() = listOf(
        ViolationsConfiguration().apply {
            minLines = 0.1
            minCoverage = 0.2
        },
        ViolationsConfiguration().apply {
            minLines = 0.1
            rules = listOf(Rule())
        },
        ViolationsConfiguration().apply {
            minLines = 0.1
            minCoverage = 0.2
            rules = listOf(Rule())
        },
        ViolationsConfiguration().apply {
            minCoverage = 0.2
            rules = listOf(Rule())
        },
    )

    @Nested
    inner class MinCoverageConfiguration {

        @Test
        fun `should build coverage rules from min coverage property`() {
            // GIVEN
            val expectedMinCoverage = 0.1
            val violationsConfiguration = ViolationsConfiguration().apply {
                minCoverage = expectedMinCoverage
            }

            // WHEN
            val actualCoverageRules: CoverageRulesConfig = coverageRulesResolver.resolveFrom(violationsConfiguration)

            // THEN
            actualCoverageRules.entitiesRules shouldContainExactly mapOf(
                CoverageEntity.BRANCH.violationRuleEntry(expectedMinCoverage),
                CoverageEntity.INSTRUCTION.violationRuleEntry(expectedMinCoverage),
                CoverageEntity.LINE.violationRuleEntry(expectedMinCoverage),
            )
        }
    }

    @Nested
    inner class RulesConfiguration {

        @Test
        fun `should build coverage rules from rules configuration`() {
            // GIVEN
            val expectedMinLines = 0.11
            val expectedMinBranches = 0.12
            val expectedMinInstructions = 0.11
            val expectedThreshold = 2
            val violationsConfiguration = ViolationsConfiguration().apply {
                rules = listOf(
                    Rule().apply {
                        coverageEntity = CoverageEntity.BRANCH
                        minCoverageRatio = expectedMinBranches
                        entityCountThreshold = expectedThreshold
                    },
                    Rule().apply {
                        coverageEntity = CoverageEntity.INSTRUCTION
                        minCoverageRatio = expectedMinInstructions
                        entityCountThreshold = expectedThreshold
                    },
                    Rule().apply {
                        coverageEntity = CoverageEntity.LINE
                        minCoverageRatio = expectedMinLines
                        entityCountThreshold = expectedThreshold
                    },
                )
            }

            // WHEN
            val actualCoverageRules: CoverageRulesConfig = coverageRulesResolver.resolveFrom(violationsConfiguration)

            // THEN
            actualCoverageRules.entitiesRules shouldContainExactly mapOf(
                CoverageEntity.BRANCH.violationRuleEntry(expectedMinBranches, expectedThreshold),
                CoverageEntity.INSTRUCTION.violationRuleEntry(expectedMinInstructions, expectedThreshold),
                CoverageEntity.LINE.violationRuleEntry(expectedMinBranches, expectedThreshold),
            )
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class LegacyPropertyConfiguration {

        @ParameterizedTest
        @MethodSource("legacy properties configuration test parameters")
        fun `should build coverage rules with rules from legacy properties`(
            // GIVEN
            violationsConfiguration: ViolationsConfiguration,
            expectedRules: Map<CoverageEntity, ViolationRule>
        ) {
            // WHEN
            val actualCoverageRules: CoverageRulesConfig = coverageRulesResolver.resolveFrom(violationsConfiguration)

            // THEN
            actualCoverageRules.entitiesRules shouldContainExactly expectedRules
        }

        private fun `legacy properties configuration test parameters`() = listOf<Arguments>(
            arguments(
                ViolationsConfiguration().apply { minLines = 0.1 },
                mapOf(CoverageEntity.LINE.violationRuleEntry(0.1))
            ),
            arguments(
                ViolationsConfiguration().apply { minBranches = 0.2 },
                mapOf(CoverageEntity.BRANCH.violationRuleEntry(0.2))
            ),
            arguments(
                ViolationsConfiguration().apply { minInstructions = 0.4 },
                mapOf(CoverageEntity.INSTRUCTION.violationRuleEntry(0.4))
            ),
            arguments(
                ViolationsConfiguration().apply { minBranches = 0.4; minLines = 0.8 },
                mapOf(
                    CoverageEntity.BRANCH.violationRuleEntry(0.4),
                    CoverageEntity.LINE.violationRuleEntry(0.8),
                )
            ),
            arguments(
                ViolationsConfiguration().apply {
                    minLines = 0.3; minBranches = 0.5; minInstructions = 0.7
                },
                mapOf(
                    CoverageEntity.LINE.violationRuleEntry(0.3),
                    CoverageEntity.BRANCH.violationRuleEntry(0.5),
                    CoverageEntity.INSTRUCTION.violationRuleEntry(0.7),
                )
            ),
        )
    }

    private fun CoverageEntity.violationRuleEntry(
        minCoverage: Double,
        minCoverageThreshold: Int? = null,
    ): Pair<CoverageEntity, ViolationRule> = this to this.violationRule(minCoverage, minCoverageThreshold)

    private fun CoverageEntity.violationRule(
        minCoverage: Double,
        minCoverageThreshold: Int? = null,
    ) = ViolationRule {
        coverageEntity = this@violationRule
        minCoverageRatio = minCoverage
        entityCountThreshold = minCoverageThreshold
    }
}
