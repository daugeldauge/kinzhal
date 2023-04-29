package com.daugeldauge.kinzhal.processor.model

import com.google.devtools.ksp.symbol.*

internal sealed interface Binding {
    val key: Key
    val declaration: KSDeclaration?
    val dependencies: List<Key>
        get() = emptyList()
}

internal class ComponentBinding(
    component: KSClassDeclaration,
) : Binding {
    override val key = Key(component.asStarProjectedType())
    override val declaration: KSDeclaration = component
}

internal class ComponentDependencyFunctionBinding(
    override val key: Key,
    override val declaration: KSFunctionDeclaration,
    val dependenciesInterface: KSType,
) : Binding

internal class ComponentDependencyPropertyBinding(
    override val key: Key,
    override val declaration: KSPropertyDeclaration,
    val dependenciesInterface: KSType,
) : Binding

internal class DelegatedBinding(
    override val key: Key,
    override val declaration: KSFunctionDeclaration,
    delegatedTo: Key,
) : Binding {
    override val dependencies = listOf(delegatedTo)
}

internal class FactoryBinding(
    override val key: Key,
    override val declaration: KSDeclaration?,
    override val dependencies: List<Key>,
    val containingFile: KSFile,
    val scoped: Boolean,
    val factoryName: String,
    val factoryPackage: String,
) : Binding
