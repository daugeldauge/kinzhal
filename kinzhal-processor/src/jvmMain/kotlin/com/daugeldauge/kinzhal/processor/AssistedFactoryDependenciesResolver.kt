package com.daugeldauge.kinzhal.processor

import com.daugeldauge.kinzhal.processor.generation.FactoryDependency
import com.daugeldauge.kinzhal.processor.generation.providerName
import com.daugeldauge.kinzhal.processor.model.AssistedFactoryType
import com.google.devtools.ksp.symbol.KSFunctionDeclaration


internal object AssistedFactoryDependenciesResolver {
    fun match(
        sourceDeclaration: KSFunctionDeclaration,
        assistedFactoryType: AssistedFactoryType,
    ): List<FactoryDependency> {
        val dependencies = sourceDeclaration.parameters.map {
            FactoryDependency.fromParameter(it)
        }

        val assistedSourceParameters = dependencies
            .asSequence()
            .filter(FactoryDependency::isAssisted)
            .map { it.providerName to it.key.type }
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

