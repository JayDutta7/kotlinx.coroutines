/*
 * Copyright 2016-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmMultifileClass
@file:JvmName("FlowKt")

package kotlinx.coroutines.flow

import kotlin.jvm.*

/**
 * Transforms the given flow into a flow of elements that match given [predicate]
 */
public fun <T : Any> Flow<T>.filter(predicate: suspend (T) -> Boolean): Flow<T> = flow {
    // TODO inliner 1.3.30
    collect { value ->
        if (predicate(value)) emit(value)
    }
}