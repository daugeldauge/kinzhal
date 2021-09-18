package com.daugeldauge.kinzhal.processor.model

import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration

internal sealed interface RequestedKey {
    val key: Key
    val declaration: KSDeclaration
}

internal class ComponentFunctionRequestedKey(
    override val key: Key,
    override val declaration: KSFunctionDeclaration,
) : RequestedKey

internal class ComponentPropertyRequestedKey(
    override val key: Key,
    override val declaration: KSPropertyDeclaration,
) : RequestedKey
