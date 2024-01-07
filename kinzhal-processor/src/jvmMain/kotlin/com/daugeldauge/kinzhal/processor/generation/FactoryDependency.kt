package com.daugeldauge.kinzhal.processor.generation

import com.daugeldauge.kinzhal.annotations.Assisted
import com.daugeldauge.kinzhal.processor.model.Key
import com.daugeldauge.kinzhal.processor.requireQualifiedName
import com.daugeldauge.kinzhal.processor.resolveToUnderlying
import com.daugeldauge.kinzhal.processor.toKey
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.LambdaTypeName

internal data class FactoryDependency(
    val parameterName: String,
    val key: Key,
    val isAssisted: Boolean,
    val laziness: Laziness,
) {
    enum class Laziness {
        None,
        Lazy,
        Provider,
    }

    companion object {
        fun fromParameter(parameter: KSValueParameter): FactoryDependency {
            val resolvedParameter = parameter.type.resolveToUnderlying()
            val isAssisted = parameter.isAssisted

            val (laziness, adjustedType) = when {
                isAssisted -> Laziness.None to parameter.type

                resolvedParameter.declaration.qualifiedName?.asString() == Lazy::class.java.name -> {
                    Laziness.Lazy to resolvedParameter.arguments.first().type!!
                }

                resolvedParameter.declaration.qualifiedName?.asString() == "kotlin.Function0" -> {
                    Laziness.Provider to resolvedParameter.arguments.first().type!!
                }

                else -> Laziness.None to parameter.type
            }
            return FactoryDependency(
                parameterName = parameter.name!!.asString(),
                key = adjustedType.toKey(parameter.annotations),
                laziness = laziness,
                isAssisted = isAssisted,
            )
        }
    }
}

internal val FactoryDependency.providerName: String
    get() = if (isAssisted) {
        parameterName
    } else {
        "${parameterName}Provider"
    }

internal val FactoryDependency.providerType: LambdaTypeName
    get() = LambdaTypeName.get(returnType = key.asTypeName())

internal fun CodeBlock.Builder.addProviderCodeBlock(dependency: FactoryDependency, transitiveProvider: Boolean) {
    if (transitiveProvider) {
        add("%N", dependency.providerName)
    } else {
        when (dependency.laziness) {
            FactoryDependency.Laziness.None -> add("%N()", dependency.providerName)
            FactoryDependency.Laziness.Lazy -> add("lazy { %N() }", dependency.providerName)
            FactoryDependency.Laziness.Provider -> add("%N", dependency.providerName)
        }
    }
}

private val KSValueParameter.isAssisted: Boolean
    get() = annotations.any {
        it.annotationType.resolve().declaration.qualifiedName?.asString() == Assisted::class.requireQualifiedName()
    }