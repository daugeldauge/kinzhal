package com.daugeldauge.kinzhal.processor

import com.daugeldauge.kinzhal.annotations.Component
import com.daugeldauge.kinzhal.annotations.Inject
import com.daugeldauge.kinzhal.annotations.Qualifier
import com.daugeldauge.kinzhal.processor.generation.*
import com.daugeldauge.kinzhal.processor.generation.asTypeName
import com.daugeldauge.kinzhal.processor.generation.capitalized
import com.daugeldauge.kinzhal.processor.generation.generateComponent
import com.daugeldauge.kinzhal.processor.generation.generateFactory
import com.daugeldauge.kinzhal.processor.model.*
import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.isConstructor
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import java.lang.IllegalStateException
import kotlin.reflect.KProperty

// TODO scope validation

class KinzhalSymbolProcessor(private val codeGenerator: CodeGenerator, private val logger: KSPLogger) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {

        val constructorInjectedBindings = resolver.getSymbolsWithAnnotation(Inject::class.requireQualifiedName())
            .mapNotNull { injectable ->
                if (injectable !is KSFunctionDeclaration || !injectable.isConstructor()) {
                    logger.error("@Inject can't be applied to $injectable: only constructor injection supported", injectable)
                    return@mapNotNull null
                }

                val injectableKey = injectable.returnTypeKey()

                generateFactory(
                    codeGenerator = codeGenerator,
                    injectableKey = injectableKey,
                    annotations = injectableKey.type.declaration.annotations,
                    sourceDeclaration = injectable,
                    addCreateInstanceCall = { add("%T", injectableKey.asTypeName()) },
                    packageName = injectableKey.type.declaration.packageName.asString(),
                    factoryName = injectableKey.type.declaration.simpleName.asString() + "Factory",
                )
            }
            .toList()

        resolver.getSymbolsWithAnnotation(Component::class.requireQualifiedName())
            .forEach { component ->

                if (component !is KSClassDeclaration || component.classKind != ClassKind.INTERFACE) {
                    logger.error("@Component can't be applied to $component: must be an interface", component)
                    return@forEach
                }

                val componentAnnotation = component.findAnnotation<Component>()!!

                val modules = componentAnnotation.typeListParameter(Component::modules).map { it.declaration as KSClassDeclaration }

                val modulesWithCompanions = modules + modules.mapNotNull { module ->
                    module.declarations.filterIsInstance<KSClassDeclaration>().find { it.isCompanionObject }
                }

                val providerBindings = modulesWithCompanions.flatMap { module ->
                    module.declarations.filterIsInstance<KSFunctionDeclaration>()
                        .filter { !it.isAbstract && !it.isConstructor() }
                        .map { providerFunction ->

                            val injectableKey = providerFunction.returnTypeKey()
                            val providerName = providerFunction.simpleName.asString()

                            generateFactory(
                                codeGenerator = codeGenerator,
                                injectableKey = injectableKey,
                                annotations = providerFunction.annotations,
                                sourceDeclaration = providerFunction,
                                addCreateInstanceCall = { add("%T.$providerName", module.asClassName()) },
                                packageName = module.packageName.asString(),
                                factoryName = ClassName.bestGuess(module.qualifiedName!!.asString()).simpleNames.joinToString(separator = "") + providerName.capitalized() + "Factory",
                            )
                        }
                }

                val delegated = modulesWithCompanions.flatMap { module ->
                    module.declarations.filterIsInstance<KSFunctionDeclaration>()
                        .filter { it.isAbstract }
                        .mapNotNull { bindingFunction ->
                            if (bindingFunction.parameters.size != 1) {
                                logger.error("Binding function must have exactly one parameter", bindingFunction)
                                return@mapNotNull null
                            }

                            val typeKey = bindingFunction.returnTypeKey()
                            val parameterKey = bindingFunction.parameters.first().toKey()

                            if (!typeKey.type.isAssignableFrom(parameterKey.type)) {
                                logger.error("Binding function return type must be assignable from parameter", bindingFunction)
                            }

                            DelegatedBinding(
                                key = typeKey,
                                declaration = bindingFunction,
                                delegatedTo = parameterKey,
                            )
                        }
                }

                val componentDependencies = componentAnnotation.typeListParameter(Component::dependencies)
                    .mapNotNull { type ->
                        val declaration = type.declaration
                        if (declaration !is KSClassDeclaration || declaration.classKind != ClassKind.INTERFACE) {
                            logger.error("Component dependency must be an interface")
                            return@mapNotNull null
                        }

                        val functions = declaration.provisionFunctions()
                            .map { ComponentDependencyFunctionBinding(it.returnTypeKey(), it, type) }

                        val properties = declaration.provisionProperties()
                            .map { ComponentDependencyPropertyBinding(it.typeKey(), it, type) }

                        ComponentDependency(
                            type = type,
                            bindings = (functions + properties).toList(),
                        )
                    }

                val requestedFunctionKeys = component.provisionFunctions()
                    .map { ComponentFunctionRequestedKey(it.returnTypeKey(), it) }

                val requestedPropertyKeys = component.provisionProperties()
                    .map { ComponentPropertyRequestedKey(it.typeKey(), it) }

                UnresolvedBindingGraph(
                    component = component,
                    factoryBindings = (providerBindings + constructorInjectedBindings),
                    componentDependencies = componentDependencies,
                    delegated = delegated,
                    requested = (requestedFunctionKeys + requestedPropertyKeys).toList(),
                ).resolve(logger).generateComponent(codeGenerator)

            }

        return emptyList()
    }

    private fun KSClassDeclaration.provisionFunctions(): Sequence<KSFunctionDeclaration> {
        return getAllFunctions()
            .filter { it.isAbstract }
            .mapNotNull {
                if (it.parameters.isNotEmpty()) {
                    logger.error("Component provision function must not have any parameters", it)
                    null
                } else {
                    it
                }
            }
    }


    private fun KSClassDeclaration.provisionProperties(): Sequence<KSPropertyDeclaration> {
        return getAllProperties()
            .filter { it.isAbstract() }
            .mapNotNull {
                if (it.isMutable) {
                    logger.error("Component provision property must be readonly", it)
                    null
                } else {
                    it
                }
            }
    }
}

class KinzhalSymbolProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return KinzhalSymbolProcessor(environment.codeGenerator, environment.logger)
    }
}


internal fun KSPropertyDeclaration.typeKey() = type.toKey(annotations)

internal fun KSValueParameter.toKey(): Key = type.toKey(annotations)

internal fun KSFunctionDeclaration.returnTypeKey(): Key = returnType!!.toKey(annotations)

internal fun KSTypeReference.toKey(annotations: Sequence<KSAnnotation>): Key {
    val qualifiers = annotations.mapNotNull {
        it.annotationType.resolveToUnderlying().takeIf { resolved ->
            resolved.declaration.findAnnotation<Qualifier>() != null
        }
    }.toList()

    if (qualifiers.size > 1) {
        throw IllegalStateException("Multiple qualifiers not permitted")
        // TODO pass logger
        // logger.error("Multiple qualifiers not permitted", this)
    }

    return Key(type = this.resolveToUnderlying(), qualifier = qualifiers.firstOrNull())
}
