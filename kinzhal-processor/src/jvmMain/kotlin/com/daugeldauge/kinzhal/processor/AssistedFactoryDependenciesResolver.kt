package com.daugeldauge.kinzhal.processor

import com.daugeldauge.kinzhal.annotations.Assisted
import com.daugeldauge.kinzhal.processor.model.AssistedFactoryType
import com.daugeldauge.kinzhal.processor.model.Key
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter

internal class AssistedFactoryDependency(
    val name: String,
    val key: Key,
    val isAssisted: Boolean,
    val parameter: KSValueParameter,
)

internal object AssistedFactoryDependenciesResolver {
    fun match(
        sourceDeclaration: KSFunctionDeclaration,
        assistedFactoryType: AssistedFactoryType,
    ): List<AssistedFactoryDependency> {
        val dependencies = sourceDeclaration.parameters.map {
            val isAssisted = it.isAssisted
            AssistedFactoryDependency(
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
            .filter(AssistedFactoryDependency::isAssisted)
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

        return dependencies
    }
}

private val KSValueParameter.isAssisted: Boolean
    get() = annotations.any {
        it.annotationType.resolve().declaration.qualifiedName?.asString() == Assisted::class.requireQualifiedName()
    }
