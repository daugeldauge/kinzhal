package com.daugeldauge.kinzhal.processor.generation

import com.daugeldauge.kinzhal.annotations.Assisted
import com.daugeldauge.kinzhal.processor.NonRecoverableProcessorException
import com.daugeldauge.kinzhal.processor.model.AssistedFactoryType
import com.daugeldauge.kinzhal.processor.model.FactoryBinding
import com.daugeldauge.kinzhal.processor.model.Key
import com.daugeldauge.kinzhal.processor.requireQualifiedName
import com.daugeldauge.kinzhal.processor.toKey
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.withIndent

internal fun generateAssistedFactory(
    codeGenerator: CodeGenerator,
    injectableKey: Key,
    sourceDeclaration: KSFunctionDeclaration,
    addCreateInstanceCall: CodeBlock.Builder.() -> Unit,
    packageName: String,
    factoryBaseName: String,
    assistedFactoryType: AssistedFactoryType,
): FactoryBinding {
    class Dependency(
        val name: String,
        val key: Key,
        val isAssisted: Boolean,
        val parameter: KSValueParameter,
    )

    val dependencies = sourceDeclaration.parameters.map {
        val isAssisted = it.isAssisted
        Dependency(
            name = if (isAssisted) {
                it.name!!.asString()
            } else {
                "${it.name!!.asString()}Provider"
            },
            key = it.type.toKey(it.annotations),
            isAssisted = isAssisted,
            parameter = it
        )
    }

    val assistedSourceParameters = dependencies
        .asSequence()
        .filter(Dependency::isAssisted)
        .map { it.name to it.key.type }
        .toList()

    val assistedFactoryParameters = assistedFactoryType.factoryMethod.parameters
        .map { it.name?.asString() to it.type.resolve() }

    if (assistedSourceParameters != assistedFactoryParameters) {
        throw NonRecoverableProcessorException(
            "Factory method in @AssistedFactory must have assisted parameters by same names, types and in the same order as @AssistedInject constructor",
            assistedFactoryType.factoryMethod
        )
    }

    val providers: List<Pair<String, TypeName>> = dependencies
        .asSequence()
        .filterNot { it.isAssisted }
        .map { dependency ->
            dependency.name to LambdaTypeName.get(returnType = dependency.key.asTypeName())
        }
        .toList()

    val containingFile = sourceDeclaration.containingFile!!

    val implName = factoryBaseName + "_Impl"
    codeGenerator.newFile(
        dependenciesAggregating = false,
        dependencies = arrayOf(containingFile),
        packageName = packageName,
        fileName = implName,
    ) {

        val properties = providers.map { (name, type) ->
            PropertySpec.builder(
                name,
                type,
                KModifier.PRIVATE,
            ).initializer(name).build()
        }

        addType(
            TypeSpec.classBuilder(implName)
                .addModifiers(KModifier.INTERNAL)
                .primaryConstructor(
                    FunSpec.constructorBuilder()
                        .addParameters(
                            providers.map { (name, type) -> ParameterSpec.builder(name, type).build() }
                        )
                        .build()
                )
                .addSuperinterface(assistedFactoryType.type.toTypeName())
                .addProperties(properties)
                .addFunction(
                    FunSpec.builder(assistedFactoryType.factoryMethod.simpleName.asString())
                        .returns(injectableKey.asTypeName())
                        .addModifiers(KModifier.OVERRIDE)
                        .addParameters(
                            assistedFactoryType.factoryMethod.parameters
                                .map {
                                    ParameterSpec.builder(
                                        name = it.name!!.asString(),
                                        type = it.type.resolve().toTypeName()
                                    ).build()
                                }
                        )
                        .addCode(CodeBlock.builder().apply {
                            add("return ")
                            addCreateInstanceCall()

                            if (dependencies.isEmpty()) {
                                add("()")
                            } else {
                                add("(\n")
                                withIndent {
                                    dependencies.forEach {
                                        add("%N", it.name)
                                        if (!it.isAssisted) {
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

    return generateFactory(
        codeGenerator = codeGenerator,
        injectableKey = Key(assistedFactoryType.type),
        scoped = true,
        sourceDeclaration = null,
        parameters = dependencies.asSequence().filterNot(Dependency::isAssisted).map(Dependency::parameter).toList(),
        containingFile = containingFile,
        addCreateInstanceCall = { add("%T", ClassName(packageName, implName)) },
        providersAreTransitive = true,
        packageName = packageName,
        factoryBaseName = implName
    )
}

private val KSValueParameter.isAssisted: Boolean
    get() = annotations.any {
        it.annotationType.resolve().declaration.qualifiedName?.asString() == Assisted::class.requireQualifiedName()
    }
