package com.github.surpsg.diffsource

import com.github.surpsg.DiffSourceConfiguration
import java.io.File
import java.net.URL

const val DEFAULT_PATCH_FILE_NAME: String = "diff.patch"

interface DiffSource {

    val sourceDescription: String
    fun pullDiff(): List<String>
    fun saveDiffTo(dir: File): File
}

internal class FileDiffSource(
    private val file: File
) : DiffSource {

    override val sourceDescription = "File: $file"

    override fun pullDiff(): List<String> {
        return if (file.exists() && file.isFile) {
            file.readLines()
        } else {
            throw RuntimeException("'$file' not a file or doesn't exist")
        }
    }

    override fun saveDiffTo(dir: File): File {
        return file.copyTo(dir.resolve(DEFAULT_PATCH_FILE_NAME), true)
    }
}

internal class UrlDiffSource(
    private val url: URL
) : DiffSource {
    override val sourceDescription = "URL: $url"

    private val diffContent: String by lazy { url.readText() }

    override fun pullDiff(): List<String> = diffContent.lines()

    override fun saveDiffTo(dir: File): File {
        dir.mkdirs()
        return dir.resolve(DEFAULT_PATCH_FILE_NAME).apply {
            println("save to $this")
            writeText(diffContent).apply {
                println("write competed")
            }
        }
    }
}

fun getDiffSource(diffConfig: DiffSourceConfiguration): DiffSource {
    val configurations = setOf(
        diffConfig.file?.let { FileDiffSource(it) },
        diffConfig.url?.let { UrlDiffSource(it) }
    ).filterNotNull()

    return if (configurations.size == 1) {
        configurations.first()
    } else {
        throw IllegalStateException("Expected only one of file or URL diff source expected but was: $diffConfig")
    }
}
