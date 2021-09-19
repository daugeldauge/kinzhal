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
