package com.daugeldauge.kinzhal.processor

import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated


class KinzhalSymbolProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return object : SymbolProcessor {
            override fun process(resolver: Resolver): List<KSAnnotated> {
                resolver.getSymbolsWithAnnotation("kotlin.Deprecated")
                    .mapNotNull { it.containingFile }
                    .forEach { file ->
                        environment.logger.warn(file.fileName)
                        environment.codeGenerator.createNewFile(
                            dependencies = Dependencies(aggregating = false, file),
                            packageName = file.packageName.asString(),
                            fileName = "Generated_" + file.fileName,
                            extensionName = "",
                        ).writer().use { it.append("// Generated for ${file.fileName}") }
                    }

                return emptyList()
            }
        }
    }
}
