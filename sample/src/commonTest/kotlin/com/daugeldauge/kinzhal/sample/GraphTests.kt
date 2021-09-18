package com.daugeldauge.kinzhal.sample

import com.daugeldauge.kinzhal.sample.graph.*

import kotlin.test.Test
import kotlin.test.assertNotSame
import kotlin.test.assertSame

class GraphTests {

    @Test
    fun `scoped deps aren't recreated in provision functions and vice versa`() {

        val externalComponent = KinzhalExternalComponent()

        val component = KinzhalAppComponent(
            appDependencies = object : AppDependencies {
                override val application = Application()
                override val versions: Versions = emptyMap()
            },
            externalComponent = externalComponent
        )

        assertSame(component.router, component.router)
        assertNotSame(component.createAuthPresenter(), component.createAuthPresenter())
        assertSame(component.versions, component.versions)

    }
}
