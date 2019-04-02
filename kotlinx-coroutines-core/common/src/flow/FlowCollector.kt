/*
 * Copyright 2016-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.coroutines.flow

import kotlin.coroutines.*

/**
 * [FlowCollector] is used as an intermediate or a terminal consumer of the flow and represents
 * an entity that is used to accept values emitted by the [Flow].
 *
 * This interface usually should not be implemented directly, but rather used as a receiver in [flow] builder when implementing a custom operator.
 * Implementations of this interface are not thread-safe.
 */
public interface FlowCollector<T> {

    /**
     * Consumes the value emitted by the upstream.
     */
    public suspend fun emit(value: T)
}
