@file:Suppress("KDocUnresolvedReference")

package com.daugeldauge.kinzhal.annotations

import kotlin.reflect.KClass

/**
 * Annotates an interface for which dependency-injected implementation
 * is to be generated from a set of [modules]. The generated class will
 * have the name of the type annotated with [Component] prepended with
 * Kinzhal. For example, `@Component interface AppComponent` will
 * produce an implementation named KinzhalAppComponent. All [dependencies]
 * will be primary constructor parameters of generated class.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class Component(val modules: Array<KClass<*>> = [], val dependencies: Array<KClass<*>> = [])

/**
 * Equivalent of [javax.inject.Inject]. Only a constructor injection is supported for now
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CONSTRUCTOR)
annotation class Inject

/**
 * Equivalent of [javax.inject.Scope]
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.ANNOTATION_CLASS)
annotation class Scope

/**
 * Equivalent of [javax.inject.Qualifier]
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.ANNOTATION_CLASS)
annotation class Qualifier

/**
 * Annotates the constructor of a type that will be created via assisted injection.
 *
 * Note that an assisted injection type cannot be scoped. In addition, assisted injection
 * requires the use of a factory annotated with [AssistedFactory].
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CONSTRUCTOR)
annotation class AssistedInject

/**
 * Annotates a parameter within an [AssistedInject]-annotated constructor.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Assisted

/**
 * Annotates an abstract class or interface used to create an instance of a type via an [AssistedInject] constructor.
 *
 * An [AssistedFactory]-annotated type must obey the following constraints:
 * * The type must be an abstract class or interface,
 * * The type must contain exactly one abstract, non-default method whose
 *    * return type must exactly match the type of an assisted injection type, and
 *    * parameters must match the exact list of [Assisted] parameters in the assisted
 *          injection type's constructor (and in the same order)
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class AssistedFactory
