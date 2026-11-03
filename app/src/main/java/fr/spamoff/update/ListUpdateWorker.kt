package fr.spamoff.update

import android.content.Context
import androidx.work.*
import fr.spamoff.SpamOffApp
import fr.spamoff.data.SettingsRepository
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class ListUpdateWorker(context: Context, params: WorkerParameters): CoroutineWorker(context, params) {
 override suspend fun doWork(): Result {
  val app = applicationContext as SpamOffApp
  if (!app.settings.state.first().autoUpdate) return Result.success()
  return try { app.repository.update(); Result.success() }
  catch (e: kotlinx.coroutines.CancellationException) { throw e }
  catch (e: Exception) { if (runAttemptCount < 3) Result.retry() else Result.failure() }
 }
}
object UpdateScheduler {
 suspend fun reconcile(context: Context, settings: SettingsRepository) {
  val wm = WorkManager.getInstance(context)
  if (!settings.state.first().autoUpdate) { wm.cancelUniqueWork("lists"); return }
  val request = PeriodicWorkRequestBuilder<ListUpdateWorker>(24, TimeUnit.HOURS)
   .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
   .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.MINUTES).build()
  wm.enqueueUniquePeriodicWork("lists", ExistingPeriodicWorkPolicy.KEEP, request)
 }
}
