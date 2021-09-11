@file:Suppress("unused")

package com.daugeldauge.kinzhal.sample

class SomeService(
    @SomeQualifier(lowerBound = 48) private val interfaceInstance: SomeInterface<*>,
) {
    fun veryImportantJob() {

    }
}

interface SomeInterface<SomeOption> {
    fun isItMarkerInterfaceOrNot(): Boolean
}

annotation class SomeQualifier(val lowerBound: Int)

sealed interface SomeOption {
    data class Natural(val max: Int) : SomeOption
    object Oblivious : SomeOption
    class Ordinary(val orNot: Boolean) : SomeOption

    @JvmInline
    value class Indifferent(val messageToHigherPower: String) : SomeOption
}

