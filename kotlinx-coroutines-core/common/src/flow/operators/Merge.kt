/*
 * Copyright 2016-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmMultifileClass
@file:JvmName("FlowKt")

package kotlinx.coroutines.flow
import kotlin.jvm.*

/**
 * Merges given sequence of flows into a single flow with no guarantees on the order.
 */
public fun <T> Iterable<Flow<T>>.merge(): Flow<T> = asFlow().flatMap { it }
