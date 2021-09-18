package com.daugeldauge.kinzhal.sample

import com.daugeldauge.kinzhal.sample.graph.AppDependencies
import com.daugeldauge.kinzhal.sample.graph.Application
import com.daugeldauge.kinzhal.sample.graph.KinzhalAppComponent

import kotlin.test.Test
import kotlin.test.assertNotSame
import kotlin.test.assertSame

class GraphTests {

    @Test
    fun `scoped deps aren't recreated in provision functions and vice versa`() {

        val component = KinzhalAppComponent(object : AppDependencies {
            override val application = Application()
        })

        assertSame(component.router, component.router)
        assertNotSame(component.createAuthPresenter(), component.createAuthPresenter())

    }
}
