package com.github.surpsg.diffsource

import com.github.surpsg.DiffSourceConfiguration
import org.junit.Test
import java.io.File
import java.lang.IllegalStateException
import java.net.URL

class DiffSourceTest {

    @Test(expected = IllegalStateException::class)
    fun `getDiffSource should throw when multiple sources are specified`() {
        val diffSource = DiffSourceConfiguration().apply {
            file = File(".")
            url = URL("http://test.com")
            git = "HEAD"
        }

        getDiffSource(File(""), diffSource)
    }

    @Test(expected = RuntimeException::class)
    fun `FileDiffSource pullDiff should throw when file is dir`() {
        FileDiffSource(File(".")).pullDiff()
    }

    @Test(expected = RuntimeException::class)
    fun `FileDiffSource pullDiff should throw when file doesn't exist`() {
        FileDiffSource(File("does/not/exist")).pullDiff()
    }
}
