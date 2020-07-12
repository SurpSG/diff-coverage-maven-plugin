package com.github.surpsg.diffsource

import org.eclipse.jgit.lib.CoreConfig
import org.junit.Assert.*
import org.junit.Test

class CrlfStrategyTest {

    @Test
    fun `getCrlf should return AutoCRLF TRUE for Windows line break`() {
        assertEquals(
            CoreConfig.AutoCRLF.TRUE,
            getCrlf("\r\n")
        )
    }

    @Test
    fun `getCrlf should return AutoCRLF INPUT for non Windows like break`() {
        assertEquals(
            CoreConfig.AutoCRLF.INPUT,
            getCrlf("\n")
        )
    }
}
