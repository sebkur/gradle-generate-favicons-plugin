package de.topobyte.gradle;

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.InvalidUserDataException

class GenerateFaviconsPlugin implements Plugin<Project> {
    void apply (Project project) {
        def extension = project.extensions.create('favicons', GenerateFaviconsPluginExtension)
        project.task('generateFavicons') {
            group = 'build'
            description = 'Generate favicon files'
            doLast {
                if (extension.input == null) {
                    throw new InvalidUserDataException("You need to specify the input file via 'input'")
                }
                println "Generating favicons from input file ${extension.input}"

                def svg = extension.input
                def dir = new File(project.buildDir, 'favicons')
                def subdir = new File(dir, 'images')
                dir.mkdirs()
                subdir.mkdirs()
                def out = 'favicon-%d.png'
                def sizes = [16, 32, 48, 64, 96]
                for (size in sizes) {
                    def filename = String.format(out, size)
                    def file = new File(subdir, filename)
                    println 'Generate icon: ' + file
                    project.exec {
                        commandLine = 'inkscape'
                        args = ['-C', '-h', size, '-e', file.toString(), svg]
                    }
                }

                def icoFile = new File(dir, 'favicon.ico')
                println 'Generate icon: ' + icoFile

                def icoFiles = []
                def icoSizes = [16, 32, 48, 64]
                for (size in icoSizes) {
                    def filename = String.format(out, size)
                    def file = new File(subdir, filename)
                    icoFiles << file.toString()
                }
                project.exec {
                    commandLine = 'convert'
                    args icoFiles
                    args icoFile.toString()
                }
            }
        }
    }
}
