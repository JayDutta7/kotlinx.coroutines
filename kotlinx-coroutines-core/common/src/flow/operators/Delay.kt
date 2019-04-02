/*
 * Copyright 2016-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmMultifileClass
@file:JvmName("FlowKt")

package kotlinx.coroutines.flow

import kotlinx.coroutines.*
import kotlin.jvm.*

/**
 * Delays the emission of values from this flow for the given [timeMillis].
 */
public fun <T : Any> Flow<T>.delayFlow(timeMillis: Long): Flow<T> = flow {
    delay(timeMillis)
    collect {
        emit(it)
    }
}

/**
 * Delays each element emitted by the given flow for the given [timeMillis].
 */
public fun <T : Any> Flow<T>.delayEach(timeMillis: Long): Flow<T> = flow {
    collect {
        delay(timeMillis)
        emit(it)
    }
}
