package com.daugeldauge.kinzhal.processor.generation

import com.daugeldauge.kinzhal.processor.AssistedFactoryDependency
import com.daugeldauge.kinzhal.processor.model.AssistedFactoryType
import com.daugeldauge.kinzhal.processor.model.FactoryBinding
import com.daugeldauge.kinzhal.processor.model.Key
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toTypeName

internal fun generateAssistedFactory(
    codeGenerator: CodeGenerator,
    injectableKey: Key,
    sourceDeclaration: KSFunctionDeclaration,
    addCreateInstanceCall: CodeBlock.Builder.() -> Unit,
    packageName: String,
    factoryBaseName: String,
    assistedFactoryType: AssistedFactoryType,
    dependencies: List<AssistedFactoryDependency>,
): FactoryBinding {
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
        parameters = dependencies.asSequence().filterNot(AssistedFactoryDependency::isAssisted).map(AssistedFactoryDependency::parameter).toList(),
        containingFile = containingFile,
        addCreateInstanceCall = { add("%T", ClassName(packageName, implName)) },
        providersAreTransitive = true,
        packageName = packageName,
        factoryBaseName = implName
    )
}
