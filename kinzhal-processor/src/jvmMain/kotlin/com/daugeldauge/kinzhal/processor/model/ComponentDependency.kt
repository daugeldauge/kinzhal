package com.daugeldauge.kinzhal.processor.model

import com.google.devtools.ksp.symbol.KSType

internal class ComponentDependency(
    val type: KSType,
    val bindings: List<Binding>,
)
