/*
 * Copyright 2016-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmMultifileClass
@file:JvmName("FlowKt")

package kotlinx.coroutines.flow

import kotlin.jvm.*

/**
 * Returns flow where all subsequent repetitions of the same value are filtered out.
 */
public fun <T> Flow<T>.distinctUntilChanged(): Flow<T> = distinctUntilChanged { it }

/**
 * Returns flow where all subsequent repetitions of the same key are filtered out, where
 * key is extracted with [keySelector] function.
 */
public fun <T, K> Flow<T>.distinctUntilChanged(keySelector: (T) -> K): Flow<T> =
    flow {
        var previousKey: K? = null
        collect { value ->
            val key = keySelector(value)
            if (previousKey != key) {
                previousKey = keySelector(value)
                emit(value)
            }
        }
    } // TODO suspend in lambda, inliner 1.3.30