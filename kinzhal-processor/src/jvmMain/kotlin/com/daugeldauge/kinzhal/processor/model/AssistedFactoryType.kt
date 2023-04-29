package com.daugeldauge.kinzhal.processor.model

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType

internal class AssistedFactoryType(
    val type: KSType,
    val factoryMethod: KSFunctionDeclaration,
)
