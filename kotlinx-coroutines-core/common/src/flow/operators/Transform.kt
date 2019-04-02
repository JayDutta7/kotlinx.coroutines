/*
 * Copyright 2016-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmMultifileClass
@file:JvmName("FlowKt")

package kotlinx.coroutines.flow

import kotlin.jvm.*
import kotlinx.coroutines.flow.unsafeFlow as flow

/**
 * Applies [transformer] function to each value of the given flow.
 * [transformer] is a generic function hat may transform emitted element, skip it or emit it multiple times.
 *
 * This operator is useless by itself, but can be used as a building block of user-specific operators:
 * ```
 * fun Flow<Int>.skipOddAndDuplicateEven(): Flow<Int> = transform { value ->
 *     if (value % 2 == 0) { // Emit only even values, but twice
 *         emit(value)
 *         emit(value)
 *     } // Do nothing if odd
 * }
 * ```
 */
public fun <T, R> Flow<T>.transform(@BuilderInference transformer: suspend FlowCollector<R>.(value: T) -> Unit): Flow<R> {
    // TODO inliner 1.3.30
    // TODO evaluate performance of operators written on top of tranform when inliner is fixed
    return flow {
        collect { value ->
            transformer(value)
        }
    }
}

/**
 * Returns a flow containing only values of the original flow that matches the given [predicate].
 */
public fun <T> Flow<T>.filter(predicate: suspend (T) -> Boolean): Flow<T> = flow {
    // TODO inliner 1.3.30
    collect { value ->
        if (predicate(value)) emit(value)
    }
}

/**
 * Returns a flow containing only values of the original flow that do not match the given [predicate].
 */
public fun <T> Flow<T>.filterNot(predicate: suspend (T) -> Boolean): Flow<T> = flow {
    // TODO inliner 1.3.30
    collect { value ->
        if (!predicate(value)) emit(value)
    }
}

/**
 * Returns a flow containing only values that are instances of specified type [R].
 */
public inline fun <reified R> Flow<*>.filterIsInstance(): Flow<R> = flow<R> {
    // TODO inliner 1.3.30
    collect { value ->
        if (value is R) emit(value)
    }
}

/**
 * Returns a flow containing only values of the original flow that are not null.
 */
public fun <T: Any> Flow<T?>.filterNotNull(): Flow<T> = flow<T> {
    // TODO inliner 1.3.30
    collect { value -> if (value != null) emit(value) }
}

/**
 * Returns a flow containing the results of applying the given [transformer] function to each value of the original flow.
 */
public fun <T, R> Flow<T>.map(transformer: suspend (value: T) -> R): Flow<R> = transform { value -> emit(transformer(value)) }

/**
 * Returns a flow that contains only non-null results of applying the given [transformer] function to each value of the original flow.
 */
public fun <T, R: Any> Flow<T>.mapNotNull(transformer: suspend (value: T) -> R?): Flow<R> = transform { value ->
    val transformed = transformer(value) ?: return@transform
    emit(transformed)
}

/**
 * Returns a flow which performs the given [action] on each value of the original flow.
 */
public fun <T> Flow<T>.onEach(action: suspend (T) -> Unit): Flow<T> = flow {
    collect { value ->
        action(value)
        emit(value)
    }
}
