package com.daugeldauge.kinzhal.processor

import com.daugeldauge.kinzhal.Component
import com.daugeldauge.kinzhal.Inject
import com.google.devtools.ksp.isConstructor
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import kotlin.reflect.KClass

class KinzhalSymbolProcessor(private val codeGenerator: CodeGenerator, private val logger: KSPLogger) : SymbolProcessor {

    var invoked = false

    override fun process(resolver: Resolver): List<KSAnnotated> {

        if (invoked) {
            return emptyList()
        } else {
            invoked = true
        }

        val commentBuilder = StringBuilder()


        commentBuilder.appendLine()
        commentBuilder.appendLine("Injectables:")


        resolver.getSymbolsWithAnnotation(Inject::class.requireQualifiedName())
            .forEach { injectable ->

                if (injectable !is KSFunctionDeclaration || !injectable.isConstructor()) {
                    logger.error("@Inject can't be applied to $injectable: only constructor injection supported", injectable)
                    return@forEach
                }

                val type = injectable.returnType!!

                val dependencies = injectable.parameters.map { it.type.resolveToUnderlying() }

                commentBuilder.appendLine("${type.resolve().declaration.simpleName.asString()} -> ${dependencies.joinToString(separator = ",") { it.declaration.simpleName.asString() }}")
            }

        commentBuilder.appendLine()
        commentBuilder.appendLine("Components:")

        resolver.getSymbolsWithAnnotation(Component::class.requireQualifiedName())
            .forEach { component ->

                if (component !is KSClassDeclaration || component.classKind != ClassKind.INTERFACE) {
                    logger.error("@Component can't be applied to $component: must be an interface", component)
                    return@forEach
                }

                commentBuilder.appendLine(component.simpleName.asString())

                logger.warn(component.annotations.joinToString { it.annotationType.toString() }, component)

                val annotation = component.annotations.find { it.annotationType.resolveToUnderlying().declaration.qualifiedName?.asString() == Component::class.qualifiedName }

                if (annotation == null) {
                    logger.error("IS NULL", component)
                    return@forEach
                }

                @Suppress("UNCHECKED_CAST")
                val modules = annotation.arguments
                    .find { it.name?.asString() == Component::modules.name }!!
                    .value as List<KSType>

                commentBuilder.appendLine(" > ${ modules.joinToString(",") { it.declaration.simpleName.asString() } }")

            }


        codeGenerator.createNewFile(Dependencies(false), "com.example.well.done", "WellDone", "kt").writer().use { writer ->
            FileSpec.builder(packageName = "com.example.well.done", fileName = "WellDone.kt")
                .addType(TypeSpec.objectBuilder("WellDone").addKdoc(commentBuilder.toString()).build())
                .build()
                .writeTo(writer)

        }

        return emptyList()
    }
}

private fun KClass<*>.requireQualifiedName() = qualifiedName!!

private fun KSTypeReference.resolveToUnderlying(): KSType {
    var candidate = resolve()
    var declaration = candidate.declaration
    while (declaration is KSTypeAlias) {
        candidate = declaration.type.resolve()
        declaration = candidate.declaration
    }
    return candidate
}


class KinzhalSymbolProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return KinzhalSymbolProcessor(environment.codeGenerator, environment.logger)
    }
}

