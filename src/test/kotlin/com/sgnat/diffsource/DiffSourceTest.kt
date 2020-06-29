package com.sgnat.diffsource

import com.sgnat.DiffSourceConfiguration
import org.junit.Assert.*
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
        }

        getDiffSource(diffSource)
    }
}
