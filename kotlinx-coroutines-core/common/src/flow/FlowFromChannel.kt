/*
 * Copyright 2016-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

/*
 * Copyright 2016-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmMultifileClass
@file:JvmName("FlowKt")

package kotlinx.coroutines.flow

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.*
import kotlin.experimental.*
import kotlin.jvm.*

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
