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

internal class ProcessorErrorsTest {

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
    fun `missing binding when only type parameters differ`() {
        expectError("Missing binding", kotlin("source.kt", """
            import com.daugeldauge.kinzhal.annotations.Component
            import com.daugeldauge.kinzhal.annotations.Inject
            
            @Component(dependencies = AppDeps::class)
            interface AppComponent {
                val map: Map<List<Int?>, Map<String, Map<List<Any?>, List<String>>>>
            }
            
            interface AppDeps {
                val map: Map<List<Int?>, Map<String, Map<List<Any>, List<String>>>>
            }
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
    fun `component is not an generic`() {
        expectError("@Component can't be generic interface", kotlin("source.kt", """
            import com.daugeldauge.kinzhal.annotations.Component
            import com.daugeldauge.kinzhal.annotations.Inject

            @Component
            interface AppComponent<T> {
                 val repo: Repo
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

    @Test
    fun `multiple qualifiers error`() {
        expectError("Multiple qualifiers not permitted", kotlin("source.kt", """
            import com.daugeldauge.kinzhal.annotations.Component
            import com.daugeldauge.kinzhal.annotations.Inject
            import com.daugeldauge.kinzhal.annotations.Qualifier            
            
            @Qualifier
            annotation class One
            
            @Qualifier
            annotation class Another
            
            @Component(modules = [AppModule::class])
            interface AppComponent {
                @One @Another
                val string: String
            }
            
            object AppModule {
                @One @Another
                fun string() = "    "
            }
        """.trimIndent()))
    }

    @Test
    fun `unresolved type error`() {
        expectError("Unable to resolve type reference: UnknownType", kotlin("source.kt", """
            import com.daugeldauge.kinzhal.annotations.Component
            import com.daugeldauge.kinzhal.annotations.Inject           
            
            @Component
            interface AppComponent {
                fun app(): App
            }
            
            class App @Inject constructor(
                unknownInstance: UnknownType
            )  
        """.trimIndent()))
    }

    @Test
    fun `assisted factory is not abstract`() {
        expectError("@AssistedFactory can be applied only to abstract types", kotlin("source.kt", """
            import com.daugeldauge.kinzhal.annotations.AssistedFactory
            
            @AssistedFactory
            class AssistedFactory
        """.trimIndent()))
    }

    @Test
    fun `assisted factory has no single abstract method`() {
        expectError("@AssistedFactory type must contain only single abstract method", kotlin("source.kt", """
            import com.daugeldauge.kinzhal.annotations.AssistedFactory
            
            @AssistedFactory
            interface AssistedFactory {
                fun build1(): String
                fun build2(): String
            }
        """.trimIndent()))
    }

    @Test
    fun `assisted inject is not applied to constructor`() {
        expectError("@AssistedInject can't be applied to notConstructor: only constructor injection supported", kotlin("source.kt", """
            import com.daugeldauge.kinzhal.annotations.AssistedInject
            
            @AssistedInject
            fun notConstructor() = Unit
        """.trimIndent()))
    }

    @Test
    fun `no factory for assisted type`() {
        expectError("@AssistedFactory annotated type doesn't exist for type TypeWithoutFactory", kotlin("source.kt", """
            import com.daugeldauge.kinzhal.annotations.Assisted
            import com.daugeldauge.kinzhal.annotations.AssistedInject
            
            class TypeWithoutFactory @AssistedInject constructor(
                @Assisted param1: Int,
                param2: String
            )
        """.trimIndent()))
    }

    @Test
    fun `failed to match assisted factory with constructor`() {
        expectError("Factory method in @AssistedFactory must have assisted parameters by same names, types and in the same order as @AssistedInject constructor", kotlin("source.kt", """
            import com.daugeldauge.kinzhal.annotations.Assisted
            import com.daugeldauge.kinzhal.annotations.AssistedFactory
            import com.daugeldauge.kinzhal.annotations.AssistedInject
            
            class AssistedType @AssistedInject constructor(
                @Assisted param1: Int,
                @Assisted param2: Int,
                param3: String
            )
            
            @AssistedFactory
            interface AssistedFactoryImpl {
                fun build(param2: Int, param1: Int): AssistedType
            }
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
