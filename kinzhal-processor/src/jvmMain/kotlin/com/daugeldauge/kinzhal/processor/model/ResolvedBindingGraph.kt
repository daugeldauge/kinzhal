package com.daugeldauge.kinzhal.processor.model

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType

internal class ResolvedBinding(
    val binding: Binding,
    val dependencies: List<Binding>,
)

internal class ResolvedRequestedKey(
    val requested: RequestedKey,
    val binding: Binding,
)

internal class ResolvedBindingGraph(
    val component: KSClassDeclaration,
    val componentDependencies: List<KSType>,
    val bindings: List<ResolvedBinding>, // topologically sorted required bindings
    val requested: List<ResolvedRequestedKey>,
)
