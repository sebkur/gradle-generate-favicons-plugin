package de.topobyte.gradle

import de.topobyte.os.meta.Software;

import java.nio.file.Files
import java.util.regex.Pattern

import org.gradle.api.InvalidUserDataException
import org.gradle.api.Plugin
import org.gradle.api.Project

class GenerateFaviconsPlugin implements Plugin<Project> {
	void apply(Project project) {
		def extension = project.extensions.create('favicons', GenerateFaviconsPluginExtension)
		project.task('generateFavicons') {
			group = 'build'
			description = 'Generate favicon files'
			doLast {
				if (extension.input == null) {
					throw new InvalidUserDataException("You need to specify the input file via 'input'")
				}

				def inkscape = Software.inkscapeVersionInfo
				if (!inkscape.installed) {
					throw new IOException("Cannot find inkscape executable")
				}
				println "Detected version $inkscape.major.$inkscape.minor.$inkscape.patch"

				println "Generating favicons from input file ${extension.input}"

				def projectDir = project.projectDir.toPath();
				def buildDir = project.buildDir.toPath()

				def svg = projectDir.resolve(extension.input)
				def dir = buildDir.resolve('favicons')
				def subdir = dir.resolve('images')
				Files.createDirectories(dir)
				Files.createDirectories(subdir)
				def out = 'favicon-%d.png'
				def sizes = [16, 32, 48, 64, 96]
				for (size in sizes) {
					def filename = String.format(out, size)
					def file = subdir.resolve(filename)
					if (!Files.exists(file) || Files.getLastModifiedTime(file) < Files.getLastModifiedTime(svg)) {
						println 'Generate icon: ' + file
						if (inkscape.major == 0) {
							project.exec {
								commandLine = 'inkscape'
								args = [
									'-C',
									'-h',
									size,
									'-e',
									file.toString(),
									svg
								]
							}
						} else if (inkscape.major == 1) {
							project.exec {
								commandLine = 'inkscape'
								args = [
									'-C',
									'-h',
									size,
									'-o',
									file.toString(),
									svg
								]
							}
						}
					} else {
						println 'Icon is up to date: ' + file
					}
				}

				def icoFile = dir.resolve('favicon.ico')

				def icoFiles = []
				def icoSizes = [16, 32, 48, 64]
				for (size in icoSizes) {
					def filename = String.format(out, size)
					def file = subdir.resolve(filename)
					icoFiles << file.toString()
				}
				if (!Files.exists(icoFile) || Files.getLastModifiedTime(icoFile) < Files.getLastModifiedTime(svg)) {
					project.exec {
						println 'Generate icon: ' + icoFile
						commandLine = 'convert'
						args icoFiles
						args icoFile.toString()
					}
				} else {
					println 'Icon is up to date: ' + icoFile
				}
			}
		}
	}
}
