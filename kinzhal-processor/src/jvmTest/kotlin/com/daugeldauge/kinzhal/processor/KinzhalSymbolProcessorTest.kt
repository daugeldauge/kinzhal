package com.daugeldauge.kinzhal.processor

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.SourceFile.Companion.kotlin
import com.tschuchort.compiletesting.symbolProcessorProviders
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import kotlin.test.assertContains
import kotlin.test.assertEquals

internal class KinzhalSymbolProcessorTest {

    @Rule
    @JvmField
    var temporaryFolder = TemporaryFolder()

    @Test
    fun `duplicate binding error`() {
        expectError("Duplicated binding", kotlin("source.kt", """
            import com.daugeldauge.kinzhal.annotations.Component
            import com.daugeldauge.kinzhal.annotations.Inject
            
            @Component(modules = [AppModule::class])
            interface AppComponent {
                val repo: Repo
            }
            
            object AppModule {
                fun provideAnotherRepo() = Repo()
            }
                
            class Repo @Inject constructor()
        """.trimIndent()))
    }

    @Test
    fun `missing binding error`() {
        expectError("Missing binding", kotlin("source.kt", """
            import com.daugeldauge.kinzhal.annotations.Component
            import com.daugeldauge.kinzhal.annotations.Inject
            
            @Component
            interface AppComponent {
                val repo: Repo
            }
            
            class Repo @Inject constructor(description: String, radix: Int)
        """.trimIndent()))
    }

    @Test
    fun `component is not an interface error`() {
        expectError("@Component can't be applied to", kotlin("source.kt", """
            import com.daugeldauge.kinzhal.annotations.Component
            import com.daugeldauge.kinzhal.annotations.Inject
            
            @Component
            abstract class AppComponent {
                abstract val repo: Repo
            }
            
            class Repo @Inject constructor()
        """.trimIndent()))
    }

    @Test
    fun `binding function with multiple params error`() {
        expectError("Binding function must have exactly one parameter", kotlin("source.kt", """
            import com.daugeldauge.kinzhal.annotations.Component
            import com.daugeldauge.kinzhal.annotations.Inject
            
            @Component(modules = [AppModule::class])
            interface AppComponent {
                val repo: Repo
            }
            
            interface AppModule {
                fun bindRepo(repo: Repo, what: Any): Any
            }
            
            class Repo
        """.trimIndent()))
    }

    @Test
    fun `binding function return type not assignable from parameter error`() {
        expectError("Binding function return type must be assignable from parameter", kotlin("source.kt", """
            import com.daugeldauge.kinzhal.annotations.Component
            import com.daugeldauge.kinzhal.annotations.Inject
            
            @Component(modules = [AppModule::class])
            interface AppComponent {
                val repo: Repo2
            }
            
            interface AppModule {
                fun bindRepo(repo: Repo2): Repo1
            }
            
            class Repo1
            class Repo2
        """.trimIndent()))
    }

    @Test
    fun `component dependency not an interface error`() {
        expectError("Component dependency must be an interface", kotlin("source.kt", """
            import com.daugeldauge.kinzhal.annotations.Component
            import com.daugeldauge.kinzhal.annotations.Inject
            
            @Component(dependencies = [AppDeps::class])
            interface AppComponent {
                val repo: Repo
            }
            
            abstract class AppDeps {
                abstract val repo: Repo
            }
            
            class Repo
        """.trimIndent()))
    }

    @Test
    fun `dependency cycle error`() {
        expectError("Dependency cycle detected", kotlin("source.kt", """
            import com.daugeldauge.kinzhal.annotations.Component
            import com.daugeldauge.kinzhal.annotations.Inject
            
            @Component(modules = [AppModule::class, LengthModule::class])
            interface AppComponent {
                val repo: Repo
            }
            
            object AppModule {
                fun repo(length: Int) = "    "
            }
            
            object LengthModule {
                fun length(repo: Repo): Int = 5
            }
                
            class Repo @Inject constructor(key: String)
        """.trimIndent()))
    }


    private fun expectError(message: String, vararg sourceFiles: SourceFile) {
        val result = compile(*sourceFiles)

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertContains(result.messages, message)
    }

    private fun prepareCompilation(vararg sourceFiles: SourceFile): KotlinCompilation {
        return KotlinCompilation()
            .apply {
                workingDir = temporaryFolder.root
                sources = sourceFiles.asList()
                inheritClassPath = true
                symbolProcessorProviders = listOf(KinzhalSymbolProcessorProvider())
            }
    }

    private fun compile(vararg sourceFiles: SourceFile): KotlinCompilation.Result {
        return prepareCompilation(*sourceFiles).compile()
    }

}
