package com.daugeldauge.kinzhal.processor.generation

import com.daugeldauge.kinzhal.annotations.Scope
import com.daugeldauge.kinzhal.processor.findAnnotation
import com.daugeldauge.kinzhal.processor.model.FactoryBinding
import com.daugeldauge.kinzhal.processor.model.Key
import com.daugeldauge.kinzhal.processor.resolveToUnderlying
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
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
        dependencies = sourceDeclaration.parameters.map {
                FactoryDependency.fromParameter(it)
        },
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
    dependencies: List<FactoryDependency>,
    containingFile: KSFile,
    addCreateInstanceCall: CodeBlock.Builder.() -> Unit,
    providersAreTransitive: Boolean,
    packageName: String,
    factoryBaseName: String,
): FactoryBinding {
    val factoryName = factoryBaseName + "_Factory"
    codeGenerator.newFile(
        dependenciesAggregating = false,
        dependencies = arrayOf(containingFile),
        packageName = packageName,
        fileName = factoryName,
    ) {

        val properties = dependencies.map { dependency ->
            PropertySpec.builder(
                dependency.providerName,
                dependency.providerType,
                KModifier.PRIVATE,
            ).initializer(dependency.providerName).build()
        }

        addType(
            TypeSpec.classBuilder(factoryName)
                .addModifiers(KModifier.INTERNAL)
                .primaryConstructor(
                    FunSpec.constructorBuilder()
                        .addParameters(
                            dependencies.map { dependency -> ParameterSpec.builder(dependency.providerName, dependency.providerType).build() }
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

                            if (dependencies.isEmpty()) {
                                add("()")
                            } else {
                                add("(\n")
                                withIndent {
                                    dependencies.forEach { dependency ->
                                        addProviderCodeBlock(dependency, providersAreTransitive)
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
        dependencies = dependencies.map { it.key },
        factoryName = factoryName,
        factoryPackage = packageName,
    )
}
