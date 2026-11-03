package fr.spamoff.ui

import android.app.Application
import androidx.room.withTransaction
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import fr.spamoff.SpamOffApp
import fr.spamoff.data.*
import fr.spamoff.update.UpdateScheduler
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class MainViewModel(application: Application): AndroidViewModel(application) {
 private val app = application as SpamOffApp
 private val repo = app.repository
 val settings = app.settings.state.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Settings())
 val people = repo.dao.people().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
 val counts = repo.dao.counts().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
 val metadata = repo.dao.metadataFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
 val rejected = repo.dao.rejected().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)
 val message = MutableStateFlow<String?>(null)
 val busy = MutableStateFlow(false)
 fun clearMessage() { message.value = null }
 private fun action(block: suspend () -> Unit) { viewModelScope.launch {
  try { withContext(Dispatchers.IO) { block() } }
  catch(e: CancellationException) { throw e }
  catch(e: Exception) { message.value = e.message ?: "Opération impossible" }
 } }
 fun enabled(v: Boolean) = action { app.settings.setEnabled(v) }
 fun automated(v: Boolean) = action { app.settings.setAutomated(v) }
 fun auto(v: Boolean) = action { app.settings.setAuto(v); UpdateScheduler.reconcile(app, app.settings) }
 fun theme(v: String) = action { app.settings.setTheme(v) }
 fun add(n: String, allow: Boolean) = action { repo.save(n, allow); message.value = "Numéro enregistré" }
 fun remove(n: String) = action { repo.db.withTransactionCompat(n); message.value = "Numéro et signalement local supprimés" }
 fun report(n: String, category: String, comment: String, block: Boolean) = action {
  repo.report(n, category, comment, block); message.value = "Signalement conservé sur ce téléphone uniquement"
 }
 fun update() {
  if (busy.value) return
  busy.value = true
  viewModelScope.launch {
   try { repo.update(); message.value = "Listes vérifiées et actualisées" }
   catch(e: CancellationException) { throw e }
   catch(e: Exception) { message.value = "Actualisation impossible. La base précédente est conservée." }
   finally { busy.value = false }
  }
 }
}
private suspend fun SpamDatabase.withTransactionCompat(n: String) = this.withTransaction { dao().remove(n); dao().removeReport(n) }
