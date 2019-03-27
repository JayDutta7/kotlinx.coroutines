/*
 * Copyright 2016-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.coroutines.flow.operators

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.builders.*
import kotlinx.coroutines.flow.terminal.*
import kotlin.test.*

class MapTest : TestBase() {
    @Test
    fun testMap() = runTest {
        val flow = flow {
            emit(1)
            emit(2)
        }

        val result = flow.map { it + 1 }.sum()
        assertEquals(5, result)
    }

    @Test
    fun testEmptyFlow() = runTest {
        val sum = flowOf<Int>().map { it + 1 }.sum()
        assertEquals(0, sum)
    }

    @Test
    fun testErrorCancelsUpstream() = runTest {
        var cancelled = false
        val latch = Channel<Unit>()
        val flow = flow {
            coroutineScope {
                launch {
                    latch.send(Unit)
                    hang { cancelled = true }
                }
                emit(1)
            }
        }.map {
            latch.receive()
            throw TestException()
            it + 1
        }.onErrorReturn(42)

        assertEquals(42, flow.single())
        assertTrue(cancelled)
    }
}
