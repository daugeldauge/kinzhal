package com.daugeldauge.kinzhal.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import java.io.OutputStream

class KinzhalSymbolProcessor(private val codeGenerator: CodeGenerator) : SymbolProcessor {


    private var invoked = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (invoked) {
            return emptyList()
        }
        invoked = true

        val out: OutputStream = codeGenerator.createNewFile(Dependencies(false), "com.example.well.done", "WellDone")

        fun write(s: String) {
            out.write((s + "\n").toByteArray())
        }

        val files = resolver.getAllFiles()

        write("package com.example.well.done")
        write("")

        write("/*")
        files.forEach { originalFile ->
            originalFile.accept(object : KSVisitorVoid() {
                override fun visitFile(file: KSFile, data: Unit) {
                    file.declarations.filterIsInstance<KSClassDeclaration>().forEach {
                        it.accept(this, Unit)
                    }
                }

                override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
                    write(classDeclaration.simpleName.asString())
                    classDeclaration.primaryConstructor?.parameters.orEmpty().forEach {
                        it.type.accept(this, Unit)
                    }
                }

                override fun visitValueParameter(valueParameter: KSValueParameter, data: Unit) {
                    write("     " + valueParameter.name?.asString())
                }
            }, Unit)
        }
        write("*/")
        write("object WellDone")
        return emptyList()
    }
}

class KinzhalSymbolProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return KinzhalSymbolProcessor(environment.codeGenerator)
    }
}

