package com.daugeldauge.kinzhal.processor

import com.daugeldauge.kinzhal.Component
import com.daugeldauge.kinzhal.Inject
import com.daugeldauge.kinzhal.Qualifier
import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.isConstructor
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

data class Key(
    val type: KSType,
    val qualifier: KSType? = null,
)

sealed interface Binding {
    val key: Key
}

class FactoryBinding(
    override val key: Key,
    val scoped: Boolean,
    val dependencies: List<Key>,
    val factoryQualifiedName: String,
) : Binding

class ComponentDependencyFunctionBinding(
    override val key: Key,
    val declaration: KSFunctionDeclaration,
) : Binding

class ComponentDependencyPropertyBinding(
    override val key: Key,
    val declaration: KSPropertyDeclaration,
) : Binding

class ComponentDependency(
    val type: KSType,
    val bindings: List<Binding>,
)

sealed interface RequestedKey {
    val key: Key
}

class ComponentFunctionRequestedKey(
    override val key: Key,
    val declaration: KSFunctionDeclaration,
) : RequestedKey

class ComponentPropertyRequestedKey(
    override val key: Key,
    val declaration: KSPropertyDeclaration,
) : RequestedKey

class DelegatedKey(
    val key: Key,
    val delegatedTo: Key,
)

class UnresolvedBindingGraph(
    val component: KSClassDeclaration,
    val factoryBindings: List<FactoryBinding>, // from constructor injected and @Provides
    val componentDependencies: List<ComponentDependency>,
    val delegated: List<DelegatedKey>, // from @Binds
    val requested: List<RequestedKey>, // component provision methods for now
)

class ResolvedBindingGraph(
    val component: KSClassDeclaration,
    val componentDependencies: List<KSType>,
    val bindings: List<Binding>, // topologically sorted required bindings
    val requestedKey: List<RequestedKey>,
)


class KinzhalSymbolProcessor(private val codeGenerator: CodeGenerator, private val logger: KSPLogger) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {

        val constructorInjectedBindings = resolver.getSymbolsWithAnnotation(Inject::class.requireQualifiedName())
            .mapNotNull { injectable ->
                if (injectable !is KSFunctionDeclaration || !injectable.isConstructor()) {
                    logger.error("@Inject can't be applied to $injectable: only constructor injection supported", injectable)
                    return@mapNotNull null
                }

                val injectableKey = injectable.returnTypeKey()

                generateFactory(injectableKey, injectable) { add("%T", injectableKey.asTypeName()) }
            }
            .toList()

        resolver.getSymbolsWithAnnotation(Component::class.requireQualifiedName())
            .forEach { component ->

                if (component !is KSClassDeclaration || component.classKind != ClassKind.INTERFACE) {
                    logger.error("@Component can't be applied to $component: must be an interface", component)
                    return@forEach
                }

                logger.warn(component.annotations.joinToString { it.annotationType.toString() }, component)

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

                            generateFactory(injectableKey,
                                providerFunction) {
                                add("%T.${providerFunction.simpleName.asString()}", module.asClassName())
                            }
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

                            val parameterKey = bindingFunction.parameters.first().type.resolveToUnderlying().toKey()
                            DelegatedKey(
                                key = bindingFunction.returnTypeKey(),
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
                            .map { ComponentDependencyFunctionBinding(it.returnTypeKey(), it) }

                        val properties = declaration.provisionProperties()
                            .map { ComponentDependencyPropertyBinding(it.typeKey(), it) }

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
                )

                val generatedComponentName = "Kinzhal${component.simpleName.asString()}"

                codeGenerator.newFile(
                    dependenciesAggregating = true,
                    dependencies = arrayOf(component.containingFile!!),
                    packageName = component.qualifiedName!!.getQualifier(),
                    fileName = generatedComponentName,
                ) {
                    addType(
                        TypeSpec.classBuilder(generatedComponentName)
                            .addSuperinterface(component.asClassName())
                            .addFunctions(
                                component.provisionFunctions().map { declaration ->
                                    FunSpec.builder(declaration.simpleName.asString())
                                        .addModifiers(KModifier.OVERRIDE)
                                        .returns(declaration.returnTypeKey().asTypeName())
                                        .addCode("return TODO()")
                                        .build()
                                }.toList()
                            )
                            .addProperties(
                                component.provisionProperties().map { declaration ->
                                    PropertySpec.builder(declaration.simpleName.asString(), declaration.type.resolveToUnderlying().toKey().asTypeName())
                                        .addModifiers(KModifier.OVERRIDE)
                                        .getter(FunSpec.getterBuilder().addCode("return TODO()").build())
                                        .build()
                                }.toList()
                            )
                            .build()
                    )
                }

            }

        return emptyList()
    }

    private fun generateFactory(
        injectableKey: Key,
        sourceDeclaration: KSFunctionDeclaration,
        addCreateInstanceCall: CodeBlock.Builder.() -> Unit,
    ): FactoryBinding {

        val packageName = injectableKey.type.declaration.packageName.asString()
        val factoryName = injectableKey.type.declaration.simpleName.asString() + "Factory"

        val dependencies = sourceDeclaration.parameters.map {
            ("${it.name!!.asString()}Provider") to it.type.resolveToUnderlying().toKey()
        }

        val providers: List<Pair<String, TypeName>> = dependencies.map { (providerName, key) ->
            providerName to LambdaTypeName.get(returnType = key.asTypeName())
        }

        codeGenerator.newFile(
            dependenciesAggregating = false,
            dependencies = listOfNotNull(sourceDeclaration.containingFile).toTypedArray(),//TODO not filter
            packageName = packageName,
            fileName = factoryName,
        ) {

            val properties = providers.map { (name, type) ->
                PropertySpec.builder(
                    name,
                    type,
                    KModifier.PRIVATE,
                ).initializer(name).build()
            }

            addType(
                TypeSpec.classBuilder(factoryName)
                    .primaryConstructor(
                        FunSpec.constructorBuilder()
                            .addParameters(
                                providers.map { (name, type) -> ParameterSpec.builder(name, type).build() }
                            )
                            .build()
                    )
                    .addSuperinterface(LambdaTypeName.get(returnType = injectableKey.asTypeName()))
                    .addProperties(properties)
                    .addFunction(
                        FunSpec.builder("invoke")
                            .returns(injectableKey.asTypeName())
                            .addModifiers(KModifier.OVERRIDE)
                            .addCode(CodeBlock.builder().apply {
                                add("return ")
                                addCreateInstanceCall()

                                if (properties.isEmpty()) {
                                    add("()")
                                } else {
                                    add("(\n")
                                    withIndent {
                                        properties.forEach {
                                            add("%N(),\n", it)
                                        }
                                    }
                                    add(")")
                                }
                            }.build())
                            .build()
                    )
                    .build()
            )
        }

        return FactoryBinding(
            key = injectableKey,
            scoped = false, //TODO,
            dependencies = dependencies.map { it.second },
            factoryQualifiedName = factoryName,
        )
    }

    private fun KSAnnotation.typeListParameter(property: KProperty<*>): List<KSType> {
        @Suppress("UNCHECKED_CAST")
        return findParameter(property)!!.value as List<KSType>
    }

    private fun KSAnnotation.findParameter(property: KProperty<*>): KSValueArgument? {
        return arguments.find { it.name?.asString() == property.name }
    }

    private fun KSPropertyDeclaration.typeKey() = type.resolveToUnderlying().toKey()

    private fun KSFunctionDeclaration.returnTypeKey() = returnType!!.resolveToUnderlying().toKey()

    private fun KSType.toKey(): Key {
        val qualifiers = annotations.mapNotNull {
            it.annotationType.resolveToUnderlying().declaration.findAnnotation<Qualifier>()?.annotationType?.resolveToUnderlying()
        }.toList()

        if (qualifiers.size > 1) {
            logger.error("Multiple qualifiers not permitted", declaration)
        }

        return Key(type = this, qualifier = qualifiers.firstOrNull())
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

private inline fun <reified T> KSAnnotated.findAnnotation(): KSAnnotation? {
    return annotations.find { it.annotationType.resolveToUnderlying().declaration.qualifiedName?.asString() == T::class.qualifiedName }
}

private inline fun CodeGenerator.newFile(
    dependenciesAggregating: Boolean,
    dependencies: Array<KSFile>,
    packageName: String,
    fileName: String,
    block: FileSpec.Builder.() -> FileSpec.Builder,
) {
    createNewFile(
        dependencies = Dependencies(aggregating = dependenciesAggregating, *dependencies),
        packageName = packageName,
        fileName = fileName,
    ).writer().use { writer ->
        FileSpec.builder(packageName = packageName, fileName = "$fileName.kt")
            .indent("    ")
            .block()
            .build()
            .writeTo(writer)
    }
}

private fun Key.asTypeName(): TypeName {
    return (type.declaration as KSClassDeclaration).asClassName() // TODO
}

private fun KSClassDeclaration.asClassName(): ClassName {
    val packageNameString = packageName.asString()
    val simpleNames = qualifiedName!!.asString().removePrefix("$packageNameString.").split(".")

    return ClassName(packageNameString, simpleNames)
}

private fun KClass<*>.requireQualifiedName() = qualifiedName!!

private fun KSType.classDeclaration() = declaration as KSClassDeclaration

private fun KSTypeReference.resolveToUnderlying(): KSType {
    var candidate = resolve()
    var declaration = candidate.declaration
    while (declaration is KSTypeAlias) {
        candidate = declaration.type.resolve()
        declaration = candidate.declaration
    }
    return candidate
}


class KinzhalSymbolProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return KinzhalSymbolProcessor(environment.codeGenerator, environment.logger)
    }
}

