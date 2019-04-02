/*
 * Copyright 2016-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmMultifileClass
@file:JvmName("FlowKt")

package kotlinx.coroutines.flow

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
