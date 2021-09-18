package com.daugeldauge.kinzhal.processor.model

import com.google.devtools.ksp.symbol.KSClassDeclaration

internal class UnresolvedBindingGraph(
    val component: KSClassDeclaration,
    val factoryBindings: List<FactoryBinding>, // from constructor injected and @Provides
    val componentDependencies: List<ComponentDependency>,
    val delegated: List<DelegatedBinding>, // from @Binds
    val requested: List<RequestedKey>, // component provision functions/properties for now
)
