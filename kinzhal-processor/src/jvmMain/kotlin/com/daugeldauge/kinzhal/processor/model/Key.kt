package com.daugeldauge.kinzhal.processor.model

import com.google.devtools.ksp.symbol.KSType

internal data class Key(
    val type: KSType,
    val qualifier: KSType? = null,
) {
    override fun toString(): String {
        return if (qualifier != null) {
            "@$qualifier $type"
        } else {
            type.toString()
        }
    }
}
