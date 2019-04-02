/*
 * Copyright 2016-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.coroutines.flow.terminal

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.builders.*
import kotlinx.coroutines.flow.terminal.*

/**
 * Returns the number of elements in this flow.
 */
public suspend fun <T : Any> Flow<T>.count(): Int  {
    var i = 0
    collect {
        ++i
    }

    return i
}

/**
 * Returns the number of elements matching the given predicate.
 */
public suspend fun <T : Any> Flow<T>.count(predicate: suspend (T) -> Boolean): Int  {
    var i = 0
    collect { value ->
        if (predicate(value)) {
            ++i
        }
    }

    return i
}
