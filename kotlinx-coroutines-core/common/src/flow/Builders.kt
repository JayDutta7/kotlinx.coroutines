/*
 * Copyright 2016-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmMultifileClass
@file:JvmName("FlowKt")

package kotlinx.coroutines.flow

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.internal.*
import kotlin.coroutines.*
import kotlin.jvm.*

/**
 * Creates flow from the given suspendable [block].
 *
 * Example of usage:
 * ```
 * fun fibonacci(): Flow<Long> = flow {
 *   emit(1L)
 *   var f1 = 1L
 *   var f2 = 1L
 *   repeat(100) {
 *     var tmp = f1
 *     f1 = f2
 *     f2 += tmp
 *     emit(f1)
 *   }
 * }
 * ```
 *
 * `emit` should happen strictly in the dispatchers of the [block] in order to preserve flow purity.
 * For example, the following code will produce [IllegalStateException]:
 * ```
 * flow {
 *   emit(1) // Ok
 *   withContext(Dispatcher.IO) {
 *       emit(2) // Will fail with ISE
 *   }
 * }
 * ```
 * If you want to switch the context where this flow is executed use [flowOn] operator.
 */
public fun <T : Any> flow(@BuilderInference block: suspend FlowCollector<T>.() -> Unit): Flow<T> {
    return object : Flow<T> {
        override suspend fun collect(collector: FlowCollector<T>) {
            SafeCollector(collector, coroutineContext[ContinuationInterceptor]).block()
        }
    }
}

/**
 * Analogue of [flow] builder that does not check a context of flow execution.
 * Used in our own operators where we trust the context of the invocation.
 */
@PublishedApi
internal fun <T : Any> unsafeFlow(@BuilderInference block: suspend FlowCollector<T>.() -> Unit): Flow<T> {
    return object : Flow<T> {
        override suspend fun collect(collector: FlowCollector<T>) {
            collector.block()
        }
    }
}

/**
 * Creates flow that produces single value from the given functional type.
 */
public fun <T : Any> (() -> T).asFlow(): Flow<T> = unsafeFlow {
    emit(invoke())
}

/**
 * Creates flow that produces single value from the given functional type.
 */
public fun <T : Any> (suspend () -> T).asFlow(): Flow<T> = unsafeFlow {
    emit(invoke())
}

/**
 * Creates flow that produces values from the given iterable.
 */
public fun <T : Any> Iterable<T>.asFlow(): Flow<T> = unsafeFlow {
    forEach { value ->
        emit(value)
    }
}

/**
 * Creates flow that produces values from the given iterable.
 */
public fun <T : Any> Iterator<T>.asFlow(): Flow<T> = unsafeFlow {
    forEach { value ->
        emit(value)
    }
}

/**
 * Creates flow that produces values from the given sequence.
 */
public fun <T : Any> Sequence<T>.asFlow(): Flow<T> = unsafeFlow {
    forEach { value ->
        emit(value)
    }
}

/**
 * Creates flow that produces values from the given array of elements.
 */
public fun <T : Any> flowOf(vararg elements: T): Flow<T> = unsafeFlow {
    for (element in elements) {
        emit(element)
    }
}

/**
 * Returns an empty flow.
 */
@Suppress("UNCHECKED_CAST")
public fun <T: Any> emptyFlow(): Flow<T> = EmptyFlow as Flow<T>

private object EmptyFlow : Flow<Nothing> {
    override suspend fun collect(collector: FlowCollector<Nothing>) = Unit
}

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

/**
 * Creates flow that produces values from the given range.
 */
public fun IntRange.asFlow(): Flow<Int> = flow {
    forEach { value ->
        emit(value)
    }
}

/**
 * Creates flow that produces values from the given range.
 */
public fun LongRange.asFlow(): Flow<Long> = flow {
    forEach { value ->
        emit(value)
    }
}

/**
 * Creates an instance of the cold [Flow] from a supplied [SendChannel].
 *
 * To control backpressure, [bufferSize] is used and matches directly the `capacity` parameter of [Channel] factory.
 * The provided channel can later be used by any external service to communicate with flow and its buffer determines
 * backpressure buffer size or its behaviour (e.g. in case when [Channel.CONFLATED] was used).
 *
 * Example of usage:
 * ```
 * fun flowFrom(api: CallbackBasedApi): Flow<Int> = flowViaChannel { channel ->
 *     val adapter = FlowSinkAdapter(channel) // implementation of callback interface
 *     api.register(adapter)
 *     channel.invokeOnClose {
 *         api.unregister(adapter)
 *     }
 * }
 * ```
 */
public fun <T : Any> flowViaChannel( // TODO bikeshed this naming?
    bufferSize: Int = 16,
    @BuilderInference block: suspend (SendChannel<T>) -> Unit
): Flow<T> {
    require(bufferSize >= 0) { "Buffer size should be positive, but was $bufferSize" }
    return flow {
        coroutineScope {
            val channel = Channel<T>(bufferSize)
            launch {
                block(channel)
            }

            channel.consumeEach { value ->
                emit(value)
            }
        }
    }
}
