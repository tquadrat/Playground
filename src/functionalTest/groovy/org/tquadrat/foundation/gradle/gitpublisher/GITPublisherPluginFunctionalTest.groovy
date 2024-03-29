/*
 * This Groovy source file was generated by the Gradle 'init' task.
 */
//file:noinspection SpellCheckingInspection
package org.tquadrat.foundation.gradle.gitpublisher

import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Files
import java.nio.file.Path

/**
 * A simple functional test for the 'org.tquadrat.foundation.gradle.gitpublisher' plugin.
 */
class GITPublisherPluginFunctionalTest extends Specification {
    @TempDir
    private File projectDir

    private getBuildFile() {
        new File( projectDir, "build.gradle" )
    }

    private getSettingsFile() {
        new File( projectDir, "settings.gradle" )
    }

    private copyProject( final File destination )
    {
        copyFolder( new File( "." ).getAbsoluteFile().toPath(), destination.getAbsoluteFile().toPath() )
    }   //  copyProject()

    private copyFolder( final Path source, final Path target )
    {
        Files.createDirectories( target )
        for( path in Files.list( source ) )
        {
            if( source.relativize(path).toString() == "settings.gradle" ) continue
            if( source.relativize(path).toString() == "build.gradle" ) continue

            var destination = target.resolve( source.relativize( path ) )
            if( Files.isDirectory( path ) )
            {
                copyFolder( path, destination )
            }
            else
            {
                Files.copy( path, destination )
            }
        }
    }   //  copyFolder()

    boolean debug = true;
    boolean dryRun = false;

    def "can run task"() {
        given:

        printf "Current Directory        : %s%n", new File( "." ).getAbsolutePath()
        printf "Project Dir for this test: %s%n", projectDir.getAbsolutePath()

        copyProject( projectDir )

//---> Begin of settings file <------------------------------------------------
        settingsFile << """
plugins {
    id  'nu.studer.credentials' version '3.0'
}

"""
//---> End of settings file <--------------------------------------------------

//---> Begin of build file <---------------------------------------------------
        buildFile << """
import java.nio.file.Files;

plugins {
    id  'nu.studer.credentials'
    id  'org.tquadrat.foundation.gradle.gitpublisher'
}

String gitUsername = credentials.forKey( 'tquadratGitHubUser' )
String gitPassword = credentials.forKey( 'tquadratGitHubPassword' )

tasks.named( 'publishToGIT' ) {
    commitMessage = "Commit"
    username = gitUsername
    password = gitPassword
    debugFlag = ${debug}
    dryRunFlag = ${dryRun}
    sourcesList = [
        "# The source files to Publish",
        "src/**"
    ]
    remoteRepositoryURI = new URI( "https://github.com/tquadrat/Playground" )
} 
"""
//---> End of build file <-----------------------------------------------------

        when:
        def runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        var param = "-PcredentialsLocation=%s/.gradle".formatted( System.getProperty( "user.home" ) )
        runner.withArguments(param, "--stacktrace", "publishToGIT" )
        runner.withProjectDir( projectDir )
        def result = runner.build()

        then:
        result.output.contains( "BUILD SUCCESSFUL in" )
    }
}