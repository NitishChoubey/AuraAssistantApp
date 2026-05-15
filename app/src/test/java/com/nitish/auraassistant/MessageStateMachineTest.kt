package com.nitish.auraassistant

import app.cash.turbine.test
import com.nitish.auraassistant.statemachine.MessageState
import com.nitish.auraassistant.statemachine.MessageStateMachine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MessageStateMachineTest {

    // ── Happy path ─────────────────────────────────────────────────────────────
    @Test
    fun `happy path transitions from Idle through to Idle`() = runTest {
        val machine = MessageStateMachine(this)

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
        val machine = MessageStateMachine(this)

        machine.state.test {
            assertEquals(MessageState.Idle, awaitItem())     // initial

            machine.sendMessage("First message")
            assertTrue(awaitItem() is MessageState.Validating)
            assertTrue(awaitItem() is MessageState.Processing)

            // Send a second message while still Processing — should cancel first
            machine.sendMessage("Second message")
            // New pipeline starts: Validating for second message
            assertTrue(awaitItem() is MessageState.Validating)
            assertTrue(awaitItem() is MessageState.Processing)
            assertTrue(awaitItem() is MessageState.Responding)
            assertEquals(MessageState.Idle, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Timeout → Error ────────────────────────────────────────────────────────
    @Test
    fun `processing timeout triggers Error state`() = runTest {
        val machine = MessageStateMachine(this)

        machine.state.test {
            assertEquals(MessageState.Idle, awaitItem())

            machine.sendMessage("Trigger timeout")

            assertTrue(awaitItem() is MessageState.Validating)
            assertTrue(awaitItem() is MessageState.Processing)

            // Advance past the 8-second timeout
            advanceTimeBy(8_500L)

            val errorState = awaitItem()
            assertTrue("Expected Error state, got $errorState", errorState is MessageState.Error)
            assertTrue((errorState as MessageState.Error).reason.contains("timed out", ignoreCase = true))

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Blank message → immediate Error ───────────────────────────────────────
    @Test
    fun `blank message moves to Error without Processing`() = runTest {
        val machine = MessageStateMachine(this)

        machine.state.test {
            assertEquals(MessageState.Idle, awaitItem())

            machine.sendMessage("   ")

            assertTrue(awaitItem() is MessageState.Validating)
            val err = awaitItem()
            assertTrue(err is MessageState.Error)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Retry from Error ───────────────────────────────────────────────────────
    @Test
    fun `retry from Error restarts pipeline`() = runTest {
        val machine = MessageStateMachine(this)

        machine.state.test {
            assertEquals(MessageState.Idle, awaitItem())

            machine.sendMessage("Trigger timeout")
            assertTrue(awaitItem() is MessageState.Validating)
            assertTrue(awaitItem() is MessageState.Processing)
            advanceTimeBy(8_500L)
            val error = awaitItem()
            assertTrue(error is MessageState.Error)

            machine.retry()
            assertTrue(awaitItem() is MessageState.Validating)
            assertTrue(awaitItem() is MessageState.Processing)
            assertTrue(awaitItem() is MessageState.Responding)
            assertEquals(MessageState.Idle, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }
}
