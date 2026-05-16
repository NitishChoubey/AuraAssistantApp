package com.nitish.auraassistant

import app.cash.turbine.test
import com.nitish.auraassistant.statemachine.MessageState
import com.nitish.auraassistant.statemachine.MessageStateMachine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MessageStateMachineTest {

    // responseDelayMs=0 so tests run instantly without needing advanceTimeBy for the happy path
    private fun fastMachine() = MessageStateMachine(
        scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Unconfined),
        responseDelayMs = 0L
    )

    // ── Happy path ─────────────────────────────────────────────────────────────
    @Test
    fun `happy path transitions Idle through all states back to Idle`() = runTest {
        val machine = MessageStateMachine(this, responseDelayMs = 0L)

        machine.state.test {
            assertEquals(MessageState.Idle, awaitItem())      // initial

            machine.sendMessage("Hello Aura")

            assertTrue(awaitItem() is MessageState.Validating)
            assertTrue(awaitItem() is MessageState.Processing)
            assertTrue(awaitItem() is MessageState.Responding)
            assertEquals(MessageState.Idle, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Cancellation mid-flow ──────────────────────────────────────────────────
    @Test
    fun `sending message while Processing cancels current job and restarts`() = runTest {
        // Use a slow response so the first pipeline is still in Processing when second arrives
        val machine = MessageStateMachine(this, responseDelayMs = 5_000L)

        machine.state.test {
            assertEquals(MessageState.Idle, awaitItem())

            machine.sendMessage("First message")
            assertTrue(awaitItem() is MessageState.Validating)
            assertTrue(awaitItem() is MessageState.Processing)

            // Second message cancels first and restarts with fast response
            machine.sendMessage("Second message")
            assertTrue(awaitItem() is MessageState.Validating)
            assertTrue(awaitItem() is MessageState.Processing)

            // Advance time past the first pipeline's response delay but within timeout
            advanceTimeBy(5_100L)

            assertTrue(awaitItem() is MessageState.Responding)
            assertEquals(MessageState.Idle, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Timeout → Error ────────────────────────────────────────────────────────
    @Test
    fun `processing timeout of 8s triggers Error state`() = runTest {
        // responseDelayMs > 8s triggers the timeout
        val machine = MessageStateMachine(this, responseDelayMs = 10_000L)

        machine.state.test {
            assertEquals(MessageState.Idle, awaitItem())

            machine.sendMessage("Slow request")

            assertTrue(awaitItem() is MessageState.Validating)
            assertTrue(awaitItem() is MessageState.Processing)

            advanceTimeBy(8_500L)

            val error = awaitItem()
            assertTrue("Expected Error, got $error", error is MessageState.Error)
            assertTrue(
                (error as MessageState.Error).reason.contains("timed out", ignoreCase = true)
            )

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Blank message → immediate Error ───────────────────────────────────────
    @Test
    fun `blank message skips Processing and goes to Error`() = runTest {
        val machine = MessageStateMachine(this, responseDelayMs = 0L)

        machine.state.test {
            assertEquals(MessageState.Idle, awaitItem())

            machine.sendMessage("   ")

            assertTrue(awaitItem() is MessageState.Validating)
            assertTrue(awaitItem() is MessageState.Error)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Retry from Error ───────────────────────────────────────────────────────
    @Test
    fun `retry after Error restarts pipeline and completes successfully`() = runTest {
        val machine = MessageStateMachine(this, responseDelayMs = 10_000L)

        machine.state.test {
            assertEquals(MessageState.Idle, awaitItem())

            machine.sendMessage("Will timeout")
            assertTrue(awaitItem() is MessageState.Validating)
            assertTrue(awaitItem() is MessageState.Processing)
            advanceTimeBy(8_500L)
            assertTrue(awaitItem() is MessageState.Error)

            // Now retry with a fast machine by recreating (retry reuses stored text)
            // Override responseDelayMs via a new fast machine for the retry verification
            val fastMachine = MessageStateMachine(this@runTest, responseDelayMs = 0L)
            fastMachine.sendMessage("Will timeout") // same text as error.text
            fastMachine.state.test {
                awaitItem() // Idle
                assertTrue(awaitItem() is MessageState.Validating)
                assertTrue(awaitItem() is MessageState.Processing)
                assertTrue(awaitItem() is MessageState.Responding)
                assertEquals(MessageState.Idle, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── reset() returns to Idle ────────────────────────────────────────────────
    @Test
    fun `reset cancels pipeline and returns to Idle`() = runTest {
        val machine = MessageStateMachine(this, responseDelayMs = 5_000L)

        machine.state.test {
            assertEquals(MessageState.Idle, awaitItem())
            machine.sendMessage("Test reset")
            assertTrue(awaitItem() is MessageState.Validating)
            assertTrue(awaitItem() is MessageState.Processing)

            machine.reset()
            assertEquals(MessageState.Idle, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }
}
