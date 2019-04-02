/*
 * Copyright 2016-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmMultifileClass
@file:JvmName("FlowKt")

package kotlinx.coroutines.flow

import kotlinx.coroutines.flow.internal.*
import kotlin.jvm.*

/**
 * Terminal operator, that awaits for one and only one value to be published.
 * Throws [NoSuchElementException] for empty flow and [IllegalStateException] for flow
 * that contains more than one element.
 */
public suspend fun <T> Flow<T>.single(): T {
    var result: Any? = NullPlaceholder
    collect { value ->
        if (result !== NullPlaceholder) error("Expected only one element")
        result = value
    }

    if (result is NullPlaceholder) throw NoSuchElementException("Expected at least one element")
    @Suppress("UNCHECKED_CAST")
    return result as T
}

/**
 * Terminal operator, that awaits for one and only one value to be published.
 * Throws [IllegalStateException] for flow that contains more than one element.
 */
public suspend fun <T: Any> Flow<T>.singleOrNull(): T? {
    var result: T? = null
    collect { value ->
        if (result != null) error("Expected only one element")
        result = value
    }

    return result
}
