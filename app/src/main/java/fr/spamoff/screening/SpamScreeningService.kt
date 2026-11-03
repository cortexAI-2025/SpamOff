package fr.spamoff.screening

import android.telecom.Call
import android.telecom.CallScreeningService
import fr.spamoff.SpamOffApp
import fr.spamoff.core.Verdict
import kotlinx.coroutines.*

class SpamScreeningService: CallScreeningService() {
 private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
 override fun onScreenCall(details: Call.Details) {
  if (details.callDirection != Call.Details.DIRECTION_INCOMING) return
  val repo = (application as SpamOffApp).repository
  // A separately supervised task lets the deadline response run without waiting on a stuck DB query.
  val task = scope.async(Dispatchers.IO) { runCatching { repo.decide(details.handle?.schemeSpecificPart) }.getOrDefault(Verdict.ALLOW) }
  scope.launch {
   val verdict = withTimeoutOrNull(2800) { task.await() } ?: Verdict.ALLOW
   task.cancel()
   val block = verdict != Verdict.ALLOW
   val response = CallResponse.Builder().setDisallowCall(block).setRejectCall(block)
    .setSkipCallLog(false).setSkipNotification(false).build()
   val sent = runCatching { respondToCall(details, response) }.isSuccess
   // Counts submitted blocking decisions only, never asserts that Telecom actually rejected the call.
   if (sent && block) (application as SpamOffApp).scope.launch { runCatching { repo.dao.increment() } }
  }
 }
 override fun onDestroy() { scope.cancel(); super.onDestroy() }
}
