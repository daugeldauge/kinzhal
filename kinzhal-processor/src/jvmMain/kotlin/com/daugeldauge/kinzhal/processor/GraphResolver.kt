package com.daugeldauge.kinzhal.processor

import com.daugeldauge.kinzhal.processor.model.Binding
import com.daugeldauge.kinzhal.processor.model.ComponentBinding
import com.daugeldauge.kinzhal.processor.model.Key
import com.daugeldauge.kinzhal.processor.model.ResolvedBinding
import com.daugeldauge.kinzhal.processor.model.ResolvedBindingGraph
import com.daugeldauge.kinzhal.processor.model.ResolvedRequestedKey
import com.daugeldauge.kinzhal.processor.model.UnresolvedBindingGraph
import com.google.devtools.ksp.processing.KSPLogger

internal fun UnresolvedBindingGraph.resolve(logger: KSPLogger): ResolvedBindingGraph {

    val allBindings = mutableMapOf<Key, Binding>()

    fun add(binding: Binding) {
        val previous = allBindings.put(binding.key, binding)

        if (previous != null) {
            logger.error("Duplicated binding: ${binding.key} already provided in ${previous.declaration?.location}", binding.declaration)
        }
    }

    add(ComponentBinding(component))

    factoryBindings.forEach(::add)

    componentDependencies.forEach { dependency ->
        dependency.bindings.forEach(::add)
    }

    delegated.forEach(::add)

    fun Key.binding() = allBindings[this] ?: throw IllegalStateException().also {
        logger.error("Missing binding: $this was not provided") // TODO add reference to requested
    }

    // TODO think about optimizing allBindings lookups

    // Topological sort
    val white = requested.map { it.key }.toMutableList()
    val grey = ArrayDeque<Key>()
    val black = linkedSetOf<Key>()

    fun tarjan(binding: Binding) {
        when {
            black.contains(binding.key) -> Unit
            grey.contains(binding.key) -> {
                grey.addLast(binding.key)
                logger.error("Dependency cycle detected: \n    ${grey.joinToString(separator = " -> ")}", binding.declaration)
            }
            else -> { // white
                white.remove(binding.key)
                grey.addLast(binding.key)
                binding.dependencies.forEach {
                    tarjan(it.binding())
                }
                grey.removeLast()
                black += binding.key
            }
        }
    }

    while (white.isNotEmpty()) {
        val current = white.last()

        tarjan(current.binding())
    }

    return ResolvedBindingGraph(
        component = component,
        componentDependencies = componentDependencies.map { it.type },
        bindings = black.map(Key::binding).map { ResolvedBinding(it, it.dependencies.map(Key::binding)) },
        requested = requested.map { ResolvedRequestedKey(it, it.key.binding()) },
    )
}
