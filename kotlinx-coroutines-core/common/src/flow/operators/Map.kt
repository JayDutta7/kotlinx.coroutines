/*
 * Copyright 2016-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmMultifileClass
@file:JvmName("FlowKt")

package kotlinx.coroutines.flow
import kotlin.jvm.*

/**
 * Transforms values emitted by the given flow with [transformer]
 *
 * TODO this method will be inline as soon as all bugs in crossinliner will be fixed
 */
public fun <T : Any, R : Any> Flow<T>.map(transformer: suspend (value: T) -> R): Flow<R> = transform { value -> emit(transformer(value)) }
