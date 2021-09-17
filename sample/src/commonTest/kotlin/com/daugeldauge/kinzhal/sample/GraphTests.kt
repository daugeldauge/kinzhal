package com.daugeldauge.kinzhal.sample

import com.daugeldauge.kinzhal.sample.graph.AppDependencies
import com.daugeldauge.kinzhal.sample.graph.Application
import com.daugeldauge.kinzhal.sample.graph.KinzhalAppComponent

import kotlin.test.Test

class GraphTests {

    @Test
    fun `scoping`() {

        val component = KinzhalAppComponent(object : AppDependencies {
            override val application = Application()
        })

        assert(component.router === component.router)
        assert(component.createAuthPresenter() === component.createAuthPresenter())

    }
}
