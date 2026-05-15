package com.nitish.auraassistant.data.sync


import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.nitish.auraassistant.data.local.db.dao.ChatMessageDao
import com.nitish.auraassistant.data.local.db.dao.UserProfileDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.TimeUnit

// ── Sync status observable from UI ───────────────────────
sealed class SyncStatus {
    object Idle : SyncStatus()
    object Syncing : SyncStatus()
    data class Success(val syncedAt: Long) : SyncStatus()
    data class Error(val message: String) : SyncStatus()
}

object SyncState {
    private val _status = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val status: StateFlow<SyncStatus> = _status.asStateFlow()

    fun update(status: SyncStatus) { _status.value = status }
}

// ── Worker ────────────────────────────────────────────────
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val chatMessageDao: ChatMessageDao,
    private val userProfileDao: UserProfileDao
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        SyncState.update(SyncStatus.Syncing)
        return try {
            val lastSyncedAt = inputData.getLong(KEY_LAST_SYNCED_AT, 0L)

            // Get only changed rows since last sync (local wins on conflict)
            val changedMessages = chatMessageDao.getChangedSince(lastSyncedAt)
            val changedProfiles = userProfileDao.getChangedSince(lastSyncedAt)

            // TODO: replace with your actual API call
            // e.g. apiService.syncMessages(changedMessages)
            // For now we simulate a successful sync
            simulateNetworkSync(changedMessages.size, changedProfiles.size)

            val now = System.currentTimeMillis()
            SyncState.update(SyncStatus.Success(now))
            Result.success(
                workDataOf(KEY_LAST_SYNCED_AT to now)
            )
        } catch (e: Exception) {
            SyncState.update(SyncStatus.Error(e.message ?: "Unknown error"))
            Result.retry()
        }
    }

    private suspend fun simulateNetworkSync(messages: Int, profiles: Int) {
        // Simulate network delay — replace with real API
        kotlinx.coroutines.delay(500)
    }

    companion object {
        const val KEY_LAST_SYNCED_AT = "last_synced_at"
    }
}

// ── Scheduler — call this from your ViewModel or App ─────
object SyncScheduler {

    fun schedulePeriodicSync(context: Context, lastSyncedAt: Long = 0L) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)   // only on network
            .build()

        val inputData = workDataOf(
            SyncWorker.KEY_LAST_SYNCED_AT to lastSyncedAt
        )

        val request = PeriodicWorkRequestBuilder<SyncWorker>(
            repeatInterval = 15,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setInputData(inputData)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "aura_sync",
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun cancelSync(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork("aura_sync")
    }
}