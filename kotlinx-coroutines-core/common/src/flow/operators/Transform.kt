/*
 * Copyright 2016-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmMultifileClass
@file:JvmName("FlowKt")

package kotlinx.coroutines.flow

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.unsafeFlow as flow // hehe
import kotlin.jvm.*

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

public fun <T> Flow<T>.filter(predicate: suspend (T) -> Boolean): Flow<T> = flow {
    // TODO inliner 1.3.30
    collect { value ->
        if (predicate(value)) emit(value)
    }
}

public fun <T, R> Flow<T>.map(transformer: suspend (value: T) -> R): Flow<R> = transform { value -> emit(transformer(value)) }

/**
 * Returns a flow which performs the given [action] on each element of the original flow.
 */
public fun <T> Flow<T>.onEach(action: suspend (T) -> Unit): Flow<T> = flow {
    collect { value ->
        action(value)
        emit(value)
    }
}

/**
 * Returns a flow that ignores first [count] elements.
 */
public fun <T> Flow<T>.drop(count: Int): Flow<T> {
    require(count >= 0) { "Drop count should be non-negative, but had $count" }
    return flow {
        var skipped = 0
        collect { value ->
            if (++skipped > count) emit(value)
        }
    }
}


/**
 * Returns a flow containing all elements except first elements that satisfy the given predicate.
 */
public fun <T> Flow<T>.dropWhile(predicate: suspend (T) -> Boolean): Flow<T> = flow {
    var matched = false
    collect { value ->
        if (matched) {
            emit(value)
        } else if (!predicate(value)) {
            matched = true
            emit(value)
        }
    }
}


/**
 * Returns a flow that contains first [count] elements.
 * When [count] elements are consumed, the original flow is cancelled.
 */
public fun <T> Flow<T>.take(count: Int): Flow<T> {
    require(count > 0) { "Take count should be positive, but had $count" }
    return flow {
        var consumed = 0
        try {
            collect { value ->
                emit(value)
                if (++consumed == count) {
                    throw TakeLimitException()
                }
            }
        } catch (e: TakeLimitException) {
            // Nothing, bail out
        }
    }
}

/**
 * Returns a flow that contains first elements satisfying the given [predicate].
 */
public fun <T> Flow<T>.takeWhile(predicate: suspend (T) -> Boolean): Flow<T> = flow {
    try {
        collect { value ->
            if (predicate(value)) emit(value)
            else throw TakeLimitException()
        }
    } catch (e: TakeLimitException) {
        // Nothing, bail out
    }
}

private class TakeLimitException : CancellationException("Flow limit is reached, cancelling") {
    // TODO expect/actual
    // override fun fillInStackTrace(): Throwable = this
}
