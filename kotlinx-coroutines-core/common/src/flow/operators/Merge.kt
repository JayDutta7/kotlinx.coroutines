/*
 * Copyright 2016-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmMultifileClass
@file:JvmName("FlowKt")
@file:Suppress("unused")

package kotlinx.coroutines.flow
import kotlinx.atomicfu.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.unsafeFlow as flow
import kotlin.jvm.*

/**
 * Transforms elements emitted by the original flow by applying [mapper], that returns another flow, and then merging and flattening these flows.
 *
 * Note that even though this operator looks very familiar, we discourage its usage in a regular application-specific flows.
 * Most likely, suspending operation in [map] operator will be sufficient and linear transformations are much easier to reason about.
 *
 * With [bufferSize] parameter, one can control the size of backpressure aka the amount of queued in-flight elements.
 */
public fun <T, R> Flow<T>.flatMap(bufferSize: Int = 16, mapper: suspend (value: T) -> Flow<R>): Flow<R> {
    /*
     * TODO open questions:
     * 1) delayErrors argument is not introduced. It is not that hard, but we need some rationale for that
     * 2) context argument is not introduced for the same reason, context is inherited from the flow
     * 3) Buffer size is not the same as `concurrency` parameter and there is no mechanism to effectively bound
     *    amount of in-flight flows
     */
    return flow {
        val flatMap = FlatMapFlow(this, bufferSize)
        coroutineScope {
            collect { outerValue ->
                val inner = mapper(outerValue)
                launch {
                    inner.collect { value ->
                        flatMap.push(value)
                    }
                }
            }
        }
    }
}

/**
 * Merges given sequence of flows into a single flow with no guarantees on the order.
 */
public fun <T> Iterable<Flow<T>>.merge(): Flow<T> = asFlow().flatMap { it }

/**
 * Concatenates values of each flow sequentially, without interleaving them.
 */
public fun <T> Flow<Flow<T>>.concatenate(): Flow<T> = kotlinx.coroutines.flow.flow {
    collect {
        val inner = it
        inner.collect { value ->
            emit(value)
        }
    }
}

/**
 * Transforms each value of the given flow into flow of another type and then flattens these flows
 * sequentially, without interleaving them.
 */
public fun <T, R> Flow<T>.concatenate(mapper: suspend (T) -> Flow<R>): Flow<R> = kotlinx.coroutines.flow.flow {
    collect { value ->
        mapper(value).collect { innerValue ->
            emit(innerValue)
        }
    }
}

private class FlatMapFlow<T>(
    private val downstream: FlowCollector<T>,
    private val bufferSize: Int
) {

    // Let's try to leverage the fact that flatMap is never contended
    private val channel: Channel<T> by lazy { Channel<T>(bufferSize) }
    private val inProgress = atomic(false)

    public suspend fun push(value: T) {
        if (!inProgress.compareAndSet(false, true)) {
            channel.send(value)
            if (inProgress.compareAndSet(false, true)) {
                helpPush()
            }
            return
        }

        downstream.emit(value)
        helpPush()
    }

    private suspend fun helpPush() {
        // TODO nulls
        var element = channel.poll()
        while (element != null) {
            downstream.emit(element)
            element = channel.poll()
        }

        inProgress.value = false
    }
}
