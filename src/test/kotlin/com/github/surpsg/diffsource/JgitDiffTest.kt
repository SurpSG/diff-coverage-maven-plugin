package com.github.surpsg.diffsource

import org.junit.Test
import java.io.File

class JgitDiffTest {

    @Test(expected = IllegalArgumentException::class)
    fun `jgit should throw when git dir not found`() {
        JgitDiff(File("unknown/file/location"))
    }

    @Test(expected = IllegalStateException::class)
    fun `jgit should throw when no such commit`() {
        JgitDiff(File(".")).obtain("UNKNOWN_COMMIT")
    }
}
