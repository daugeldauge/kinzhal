package com.daugeldauge.kinzhal.processor

import com.google.devtools.ksp.symbol.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

internal fun KClass<*>.requireQualifiedName() = qualifiedName!!

internal fun KSType.classDeclaration() = declaration as KSClassDeclaration

internal fun KSTypeReference.resolveToUnderlying(): KSType {
    var candidate = resolve()
    var declaration = candidate.declaration
    while (declaration is KSTypeAlias) {
        candidate = declaration.type.resolve()
        declaration = candidate.declaration
    }
    if (candidate.isError) {
        throw NonRecoverableProcessorException("Unable to resolve type reference: $this", node = this)
    }
    return candidate
}

internal inline fun <reified T> KSAnnotated.findAnnotation(): KSAnnotation? {
    return annotations.find { it.annotationType.resolveToUnderlying().declaration.qualifiedName?.asString() == T::class.qualifiedName }
}

internal fun KSAnnotation.typeListParameter(property: KProperty<*>): List<KSType> {
    @Suppress("UNCHECKED_CAST")
    return (findParameter(property)?.value as? List<KSType>).orEmpty()
}

internal fun KSAnnotation.findParameter(property: KProperty<*>): KSValueArgument? {
    return arguments.find { it.name?.asString() == property.name }
}

