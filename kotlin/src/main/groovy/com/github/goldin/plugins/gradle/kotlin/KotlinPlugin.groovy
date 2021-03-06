package com.github.goldin.plugins.gradle.kotlin

import org.gcontracts.annotations.Requires
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileTreeElement
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.jetbrains.jet.cli.jvm.K2JVMCompilerArguments


/**
 * Gradle Kotlin plugin.
 */
class KotlinPlugin implements Plugin<Project>
{
    static final String KDOC_TASK_NAME = 'kdoc'
    static final String EXTENSION_NAME = 'kotlin'


    @Requires({ project })
    @Override
    void apply( Project project )
    {
        final javaBasePlugin       = project.plugins.apply( JavaBasePlugin )
        final javaPluginConvention = project.convention.getPlugin( JavaPluginConvention )

        project.extensions.create  ( EXTENSION_NAME, K2JVMCompilerArguments )
        project.plugins.apply      ( JavaPlugin )
        configureSourceSetDefaults ( project, javaPluginConvention, javaBasePlugin );
        configureKDoc              ( project, javaPluginConvention );
    }


    @Requires({ project && javaPluginConvention && javaBasePlugin })
    private void configureSourceSetDefaults( Project              project,
                                             JavaPluginConvention javaPluginConvention,
                                             JavaBasePlugin       javaBasePlugin )
    {
        javaPluginConvention.sourceSets.all {
            SourceSet sourceSet ->

            sourceSet.convention.plugins.kotlin = new KotlinSourceSetImpl( sourceSet.displayName, project.fileResolver )

            // "src/main/kotlin", "src/test/kotlin"
            sourceSet.kotlin.srcDir   { project.file( "src/${ sourceSet.name }/kotlin" ) }
            sourceSet.allJava.source  ( sourceSet.kotlin )
            sourceSet.allSource.source( sourceSet.kotlin )

            sourceSet.resources.filter.exclude { FileTreeElement elem -> sourceSet.kotlin.contains( elem.file ) }

            // "compileKotlin", "compileTestKotlin"
            String            kotlinTaskName = sourceSet.getCompileTaskName( 'kotlin' )
            KotlinCompileTask kotlinTask     = project.tasks.add( kotlinTaskName, KotlinCompileTask )

            javaBasePlugin.configureForSourceSet( sourceSet, kotlinTask )

            kotlinTask.description = "Compiles the $sourceSet.kotlin."
            kotlinTask.source      = sourceSet.kotlin
        }
    }


    @Requires({ project && javaPluginConvention })
    private void configureKDoc( Project              project,
                                JavaPluginConvention javaPluginConvention )
    {
        SourceSet mainSourceSet = javaPluginConvention.sourceSets.getByName( SourceSet.MAIN_SOURCE_SET_NAME )
        KotlinKDocTask kdoc     = project.tasks.add( KDOC_TASK_NAME, KotlinKDocTask )
        kdoc.description        = 'Generates KDoc API documentation for the main source code.'
        kdoc.group              = JavaBasePlugin.DOCUMENTATION_GROUP;
        kdoc.source             = mainSourceSet.kotlin

        project.tasks.withType( KotlinKDocTask, {
            KotlinKDocTask param ->
            param.conventionMapping.map( 'destinationDir', { new File( javaPluginConvention.docsDir, 'kdoc' ) })
        })
    }
}
