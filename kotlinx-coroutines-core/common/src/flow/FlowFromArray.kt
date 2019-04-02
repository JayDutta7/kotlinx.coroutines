/*
 * Copyright 2016-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmMultifileClass
@file:JvmName("FlowKt")

package kotlinx.coroutines.flow

import kotlin.jvm.*

/**
 * Creates flow that produces values from the given array.
 */
public fun <T: Any> Array<T>.asFlow(): Flow<T> = flow {
    forEach { value ->
        emit(value)
    }
}

/**
 * Creates flow that produces values from the given array.
 */
public fun IntArray.asFlow(): Flow<Int> = flow {
    forEach { value ->
        emit(value)
    }
}

/**
 * Creates flow that produces values from the given array.
 */
public fun LongArray.asFlow(): Flow<Long> = flow {
    forEach { value ->
        emit(value)
    }
}
