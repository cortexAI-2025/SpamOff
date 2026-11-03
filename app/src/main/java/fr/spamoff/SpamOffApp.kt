package fr.spamoff

import android.app.Application
import androidx.room.Room
import fr.spamoff.data.*
import fr.spamoff.update.UpdateScheduler
import kotlinx.coroutines.*

class SpamOffApp: Application() {
 val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
 val settings by lazy { SettingsRepository(this) }
 val repository by lazy { SpamRepository(this, Room.databaseBuilder(this, SpamDatabase::class.java, "spamoff.db").build(), settings) }
 override fun onCreate() {
  super.onCreate()
  scope.launch { runCatching { repository.seed(); UpdateScheduler.reconcile(this@SpamOffApp, settings) } }
 }
}
