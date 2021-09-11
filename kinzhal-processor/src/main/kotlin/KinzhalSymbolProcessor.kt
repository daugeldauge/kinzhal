package com.daugeldauge.kinzhal.processor

import com.daugeldauge.kinzhal.Component
import com.daugeldauge.kinzhal.Inject
import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.isConstructor
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import kotlin.reflect.KClass

// TODO handle generics
// TODO support qualifiers

data class Binding(
    val qualifiedName: String,
    val qualifier: String? = null,
)

class KinzhalSymbolProcessor(private val codeGenerator: CodeGenerator, private val logger: KSPLogger) :
    SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {

        val constructorInjectedBindings = resolver.getSymbolsWithAnnotation(Inject::class.requireQualifiedName())
            .mapNotNull { injectable ->
                if (injectable !is KSFunctionDeclaration || !injectable.isConstructor()) {
                    logger.error("@Inject can't be applied to $injectable: only constructor injection supported",
                        injectable)
                    return@mapNotNull null
                }

                val injectableType = injectable.returnType!!.resolveToUnderlying()
                val injectableClassName = (injectableType.declaration as KSClassDeclaration).asClassName()

                val dependencies = injectable.parameters.map { it.type.resolveToUnderlying() }

                val packageName = injectableType.declaration.packageName.asString()
                val factoryName = injectableType.declaration.simpleName.asString() + "Factory"


                val providers: List<Pair<String, TypeName>> = injectable.parameters.map {
                    ("${it.name!!.asString()}Provider") to LambdaTypeName.get(returnType = (it.type.resolveToUnderlying().declaration as KSClassDeclaration).asClassName())
                }

                codeGenerator.newFile(
                    dependenciesAggregating = false,
                    dependencies = arrayOf(injectable.containingFile!!),
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
                            .addSuperinterface(LambdaTypeName.get(returnType = injectableClassName))
                            .addProperties(properties)
                            .addFunction(
                                FunSpec.builder("invoke")
                                    .returns(injectableClassName)
                                    .addModifiers(KModifier.OVERRIDE)
                                    .addCode(CodeBlock.builder().apply {
                                        if (properties.isEmpty()) {
                                            add("return %T()", injectableClassName)
                                        } else {
                                            add("return %T(\n", injectableClassName)
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

                injectableType.toBinding() to dependencies.map { it.toBinding() }
            }
            .toMap()

        resolver.getSymbolsWithAnnotation(Component::class.requireQualifiedName())
            .forEach { component ->

                if (component !is KSClassDeclaration || component.classKind != ClassKind.INTERFACE) {
                    logger.error("@Component can't be applied to $component: must be an interface", component)
                    return@forEach
                }

                logger.warn(component.annotations.joinToString { it.annotationType.toString() }, component)

                val annotation =
                    component.annotations.find { it.annotationType.resolveToUnderlying().declaration.qualifiedName?.asString() == Component::class.qualifiedName }!!

                @Suppress("UNCHECKED_CAST")
                val modules = annotation.arguments
                    .find { it.name?.asString() == Component::modules.name }!!
                    .value as List<KSType>

                val componentName = "Kinzhal${component.simpleName.asString()}"

                val provisionMethods = component.getAllFunctions()
                    .filter { it.isAbstract }

                val provisionProperties = component.getAllProperties()
                    .filter { it.isAbstract() }

                codeGenerator.newFile(
                    dependenciesAggregating = true,
                    dependencies = arrayOf(component.containingFile!!),
                    packageName = component.qualifiedName!!.getQualifier(),
                    fileName = componentName,
                ) {
                    addType(
                        TypeSpec.classBuilder(componentName)
                            .addSuperinterface(component.asClassName())
                            .addFunctions(
                                provisionMethods.mapNotNull { declaration ->
                                    if (declaration.parameters.isEmpty()) {
                                        FunSpec.builder(declaration.simpleName.asString())
                                            .addModifiers(KModifier.OVERRIDE)
                                            .returns((declaration.returnType!!.resolveToUnderlying().declaration as KSClassDeclaration).asClassName())
                                            .addCode("return TODO()")
                                            .build()
                                    } else {
                                        logger.error("Provision methods must not have any parameters", declaration)
                                        null
                                    }
                                }.toList()
                            )
                            .addProperties(
                                provisionProperties.mapNotNull { declaration ->
                                    if (!declaration.isMutable) {
                                        PropertySpec.builder(declaration.simpleName.asString(), (declaration.type.resolveToUnderlying().declaration as KSClassDeclaration).asClassName())
                                            .addModifiers(KModifier.OVERRIDE)
                                            .getter(FunSpec.getterBuilder().addCode("return TODO()").build())
                                            .build()
                                    } else {
                                        logger.error("Provision property must not be mutable", declaration)
                                        null
                                    }
                                }.toList()
                            )
                            .build()
                    )
                }

            }

        return emptyList()
    }
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

private fun KSClassDeclaration.asClassName(): ClassName {
    val packageNameString = packageName.asString()
    val simpleNames = qualifiedName!!.asString().removePrefix("$packageNameString.").split(".")

    return ClassName(packageNameString, simpleNames)
}

private fun KClass<*>.requireQualifiedName() = qualifiedName!!

private fun KSType.toBinding() = Binding(declaration.qualifiedName!!.asString())

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

