package com.daugeldauge.kinzhal.processor.generation

import com.daugeldauge.kinzhal.processor.model.Binding
import com.daugeldauge.kinzhal.processor.model.ComponentBinding
import com.daugeldauge.kinzhal.processor.model.ComponentDependencyFunctionBinding
import com.daugeldauge.kinzhal.processor.model.ComponentDependencyPropertyBinding
import com.daugeldauge.kinzhal.processor.model.DelegatedBinding
import com.daugeldauge.kinzhal.processor.model.FactoryBinding
import com.daugeldauge.kinzhal.processor.model.Key
import com.daugeldauge.kinzhal.processor.model.ResolvedBinding
import com.daugeldauge.kinzhal.processor.resolveToUnderlying
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Nullability
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.lang.StringBuilder


internal inline fun CodeGenerator.newFile(
    dependenciesAggregating: Boolean,
    dependencies: Array<KSFile>,
    packageName: String,
    fileName: String,
    block: FileSpec.Builder.() -> FileSpec.Builder,
) {
    createNewFile(
        dependencies = Dependencies(aggregating = dependenciesAggregating, *dependencies),
        packageName = packageName,
        fileName = fileName,
    ).writer().use { writer ->
        FileSpec.builder(packageName = packageName, fileName = "$fileName.kt")
            .indent("    ")
            .block()
            .build()
            .writeTo(writer)
    }
}

internal fun ResolvedBinding.asPropertySpec(): PropertySpec? {
    val name = binding.componentProviderName() ?: return null

    val keyType = binding.key.asTypeName()
    val type = when {
        binding is FactoryBinding && binding.scoped -> Lazy::class.asTypeName().parameterizedBy(keyType)
        else -> LambdaTypeName.get(returnType = keyType)
    }
    return PropertySpec.builder(name, type, KModifier.PRIVATE)
        .initializer(providerInitializer())
        .build()
}

private fun Binding.componentProviderName(): String? {
    return when (this) {
        is FactoryBinding -> componentProviderName()
        is DelegatedBinding -> componentProviderName()
        is ComponentDependencyFunctionBinding -> null
        is ComponentDependencyPropertyBinding -> null
        is ComponentBinding -> null
    }
}

private fun StringBuilder.appendComponentProviderName(type: KSType) {

    append(type.declaration.simpleName.asString())

    if (type.nullability == Nullability.NULLABLE) {
        append("Nullable")
    }

    type.arguments.asSequence().mapNotNull { it.type }.forEach {
        appendComponentProviderName(it.resolveToUnderlying())
    }
}

private fun StringBuilder.appendComponentProviderName(key: Key) {
    appendComponentProviderName(key.type)

    if (key.qualifier != null) {
        append(key.qualifier.declaration.simpleName.asString())
    }
}

private fun DelegatedBinding.componentProviderName(): String = buildString {
    appendComponentProviderName(key)

    append("Provider")
}.decapitalized()

private fun FactoryBinding.componentProviderName(): String = buildString {
    appendComponentProviderName(key)

    append(if (scoped) "Lazy" else "Provider")
}.decapitalized()

private fun ResolvedBinding.providerInitializer(): CodeBlock {
    return when (binding) {
        is FactoryBinding -> {
            val factoryCall = "%T" + dependencies.joinToString(separator = ", ", prefix = "(", postfix = ")") { it.providerReference() }
            CodeBlock.of(if (binding.scoped) "lazy($factoryCall)" else factoryCall, ClassName(binding.factoryPackage, binding.factoryName))
        }
        is DelegatedBinding -> CodeBlock.of(dependencies.first().providerReference())
        is ComponentDependencyFunctionBinding -> error("impossible")
        is ComponentDependencyPropertyBinding -> error("impossible")
        is ComponentBinding -> error("impossible")
    }
}

internal fun Binding.providerReference(): String {
    return when (this) {
        is ComponentBinding -> "::${GenerationConstants.SelfFunName}"
        is FactoryBinding -> if (scoped) "${componentProviderName()}::value" else componentProviderName()
        is DelegatedBinding -> componentProviderName()
        is ComponentDependencyFunctionBinding -> "${dependenciesInterface.componentDependencyPropertyName()}::${declaration.simpleName.asString()}"
        is ComponentDependencyPropertyBinding -> "${dependenciesInterface.componentDependencyPropertyName()}::${declaration.simpleName.asString()}"
    }
}

internal fun KSType.componentDependencyPropertyName() = declaration.simpleName.asString().decapitalized()

internal fun KSClassDeclaration.asClassName(): ClassName {
    val packageNameString = packageName.asString()
    val simpleNames = qualifiedName!!.asString().removePrefix("$packageNameString.").split(".")

    return ClassName(packageNameString, simpleNames)
}
internal fun Key.asTypeName(): TypeName {
    return type.asTypeName()
}

private fun KSType.asTypeName(): TypeName {
    val className = (declaration as KSClassDeclaration).asClassName()

    return if (arguments.isNotEmpty()) {
        className.parameterizedBy(arguments.mapNotNull {
            it.type?.resolveToUnderlying()?.asTypeName()
        })
    } else {
        className
    }.copy(nullable = nullability == Nullability.NULLABLE)
}

internal fun String.capitalized(): String = replaceFirstChar { it.uppercase() }
internal fun String.decapitalized(): String = replaceFirstChar { it.lowercase() }
