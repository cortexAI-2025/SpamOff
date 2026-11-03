package fr.spamoff

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** Driven only by scripts/emulator_calls.py after the ordinary integration suite. */
@RunWith(AndroidJUnit4::class)
class ScreeningHarnessTest {
 @Test fun scenario() = runBlocking {
  val app=InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as SpamOffApp
  val phase=InstrumentationRegistry.getArguments().getString("phase")
  val prefs=app.getSharedPreferences("emulator-test",0)
  val number="+33612345678"
  when(phase) {
   "block", "allow", "disabled" -> {
    app.repository.seed()
    app.repository.save(number,phase=="allow")
    app.settings.setEnabled(phase!="disabled")
    app.settings.setAutomated(false)
    prefs.edit().putLong("baseline",app.repository.dao.rejected().first() ?: 0).commit()
   }
   "assert-block" -> {
    val baseline=prefs.getLong("baseline",0)
    val value=withTimeout(5000) { app.repository.dao.rejected().first { (it ?: 0) > baseline } }
    assertEquals(baseline+1,value)
   }
   "assert-allow" -> assertEquals(prefs.getLong("baseline",0),app.repository.dao.rejected().first() ?: 0)
   "cleanup" -> { app.repository.dao.remove(number); app.settings.setEnabled(true); prefs.edit().clear().commit() }
   else -> assertNotNull(app.repository)
  }
 }
}
