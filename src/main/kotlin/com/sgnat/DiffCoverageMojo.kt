package com.sgnat

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import java.io.File
import java.io.FileWriter
import java.io.IOException


/**
 * Goal which touches a timestamp file.
 *
 * @goal touch
 *
 * @phase process-sources
 */

@Mojo(name = "diff-coverage", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
class DiffCoverageMojo : AbstractMojo() {
    /**
     * Location of the file.
     * @parameter expression="${project.build.directory}"
     * @required
     */
    @Parameter(defaultValue = "\${project.build.directory}", required = true, readonly = true)
    private lateinit var outputDirectory: File

    override fun execute() {
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs()
        }
        val touch = File(outputDirectory, "touch.txt")
        try {
            FileWriter(touch).use { it.write("touch.txt") }
        } catch (e: IOException) {
            throw MojoExecutionException("Error creating file $touch", e)
        }
    }
}
