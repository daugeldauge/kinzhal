package com.daugeldauge.kinzhal.processor.generation

import com.daugeldauge.kinzhal.processor.*
import com.daugeldauge.kinzhal.processor.model.ComponentFunctionRequestedKey
import com.daugeldauge.kinzhal.processor.model.ComponentPropertyRequestedKey
import com.daugeldauge.kinzhal.processor.model.ResolvedBindingGraph
import com.google.devtools.ksp.processing.CodeGenerator
import com.squareup.kotlinpoet.*

internal fun ResolvedBindingGraph.generateComponent(codeGenerator: CodeGenerator) {
    val generatedComponentName = "Kinzhal${component.simpleName.asString()}"

    val constructorProperties = componentDependencies.map {
        val name = it.componentDependencyPropertyName()
        val typeName = it.classDeclaration().asClassName()
        name to typeName
    }

    codeGenerator.newFile(
        dependenciesAggregating = true, // TODO is it true?
        dependencies = arrayOf(component.containingFile!!), // TODO should add files with constructor injections?
        packageName = component.qualifiedName!!.getQualifier(),
        fileName = generatedComponentName,
    ) {
        addType(
            TypeSpec.classBuilder(generatedComponentName)
                .addSuperinterface(component.asClassName())
                .primaryConstructor(
                    FunSpec.constructorBuilder()
                        .addParameters(constructorProperties.map { (name, type) -> ParameterSpec(name, type) })
                        .build()
                )
                .addProperties(
                    constructorProperties.map { (name, type) ->
                        PropertySpec.builder(name, type, KModifier.PRIVATE)
                            .initializer(name)
                            .build()
                    }
                )
                .addProperties(bindings.mapNotNull { it.asPropertySpec() })
                .apply {
                    requested.forEach { resolved ->
                        val providerReference = resolved.binding.providerReference()
                        val providerReferenceToCall = if (providerReference.contains("::")) {
                            "($providerReference)"
                        } else {
                            providerReference
                        }
                        val body = "return $providerReferenceToCall()"
                        val name = resolved.requested.declaration.simpleName.asString()

                        when (resolved.requested) {
                            is ComponentFunctionRequestedKey -> {
                                addFunction(
                                    FunSpec.builder(name)
                                        .addModifiers(KModifier.OVERRIDE)
                                        .returns(resolved.requested.declaration.returnTypeKey().asTypeName())
                                        .addCode(body)
                                        .build()
                                )
                            }
                            is ComponentPropertyRequestedKey -> addProperty(
                                PropertySpec.builder(name, resolved.requested.declaration.typeKey().asTypeName())
                                    .addModifiers(KModifier.OVERRIDE)
                                    .getter(FunSpec.getterBuilder().addCode(body).build())
                                    .build()
                            )
                        }

                    }
                }
                .build()
        )
    }
}
