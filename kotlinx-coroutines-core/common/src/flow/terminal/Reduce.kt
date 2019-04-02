/*
 * Copyright 2016-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmMultifileClass
@file:JvmName("FlowKt")

package kotlinx.coroutines.flow

import kotlin.jvm.*

/**
 * Accumulates value starting with the first element and applying [operation] to current accumulator value and each element.
 * Throws [UnsupportedOperationException] if flow was empty.
 */
public suspend fun <S : Any, T : S> Flow<T>.reduce(operation: suspend (accumulator: S, value: T) -> S): S {
    var accumulator: S? = null

    collect { value ->
        accumulator = if (accumulator != null) {
            operation(accumulator as S, value)
        } else {
            value
        }
    }

    return accumulator ?: throw UnsupportedOperationException("Empty flow can't be reduced")
}
