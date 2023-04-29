package com.daugeldauge.kinzhal.processor.generation

import com.daugeldauge.kinzhal.annotations.Scope
import com.daugeldauge.kinzhal.processor.findAnnotation
import com.daugeldauge.kinzhal.processor.model.FactoryBinding
import com.daugeldauge.kinzhal.processor.model.Key
import com.daugeldauge.kinzhal.processor.resolveToUnderlying
import com.daugeldauge.kinzhal.processor.toKey
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.*


internal fun generateFactory(
    codeGenerator: CodeGenerator,
    injectableKey: Key,
    annotations: Sequence<KSAnnotation>,
    sourceDeclaration: KSFunctionDeclaration,
    addCreateInstanceCall: CodeBlock.Builder.() -> Unit,
    packageName: String,
    factoryBaseName: String,
): FactoryBinding {
    return generateFactory(
        codeGenerator = codeGenerator,
        injectableKey = injectableKey,
        scoped = annotations.mapNotNull {
            it.annotationType.resolveToUnderlying().declaration.findAnnotation<Scope>()?.annotationType?.resolveToUnderlying()
        }.toList().isNotEmpty(),
        sourceDeclaration = sourceDeclaration,
        parameters = sourceDeclaration.parameters,
        containingFile = sourceDeclaration.containingFile!!,
        addCreateInstanceCall = addCreateInstanceCall,
        providersAreTransitive = false,
        packageName = packageName,
        factoryBaseName = factoryBaseName
    )
}

internal fun generateFactory(
    codeGenerator: CodeGenerator,
    injectableKey: Key,
    scoped: Boolean,
    sourceDeclaration: KSFunctionDeclaration?,
    parameters: List<KSValueParameter>,
    containingFile: KSFile,
    addCreateInstanceCall: CodeBlock.Builder.() -> Unit,
    providersAreTransitive: Boolean,
    packageName: String,
    factoryBaseName: String,
): FactoryBinding {
    val dependencies = parameters.map {
        ("${it.name!!.asString()}Provider") to it.type.toKey(it.annotations)
    }

    val providers: List<Pair<String, TypeName>> = dependencies.map { (providerName, key) ->
        providerName to LambdaTypeName.get(returnType = key.asTypeName())
    }

    val factoryName = factoryBaseName + "_Factory"
    codeGenerator.newFile(
        dependenciesAggregating = false,
        dependencies = arrayOf(containingFile),
        packageName = packageName,
        fileName = factoryName,
    ) {

        val properties = providers.map { (name, type) ->
            PropertySpec.builder(
                name,
                type,
                KModifier.PRIVATE,
            ).initializer(name).build()
        }

        addType(
            TypeSpec.classBuilder(factoryName)
                .addModifiers(KModifier.INTERNAL)
                .primaryConstructor(
                    FunSpec.constructorBuilder()
                        .addParameters(
                            providers.map { (name, type) -> ParameterSpec.builder(name, type).build() }
                        )
                        .build()
                )
                .addSuperinterface(LambdaTypeName.get(returnType = injectableKey.asTypeName()))
                .addProperties(properties)
                .addFunction(
                    FunSpec.builder("invoke")
                        .returns(injectableKey.asTypeName())
                        .addModifiers(KModifier.OVERRIDE)
                        .addCode(CodeBlock.builder().apply {
                            add("return ")
                            addCreateInstanceCall()

                            if (properties.isEmpty()) {
                                add("()")
                            } else {
                                add("(\n")
                                withIndent {
                                    properties.forEach {
                                        add("%N", it)
                                        if (!providersAreTransitive) {
                                            add("()")
                                        }
                                        add(",\n")
                                    }
                                }
                                add(")")
                            }
                        }.build())
                        .build()
                )
                .build()
        )
    }

    return FactoryBinding(
        key = injectableKey,
        declaration = sourceDeclaration,
        containingFile = containingFile,
        scoped = scoped,
        dependencies = dependencies.map { it.second },
        factoryName = factoryName,
        factoryPackage = packageName,
    )
}
