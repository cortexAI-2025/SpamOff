package fr.spamoff

import android.app.role.RoleManager
import android.graphics.Bitmap
import android.os.Build
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class ActivitySmokeTest {
 @Test fun activityLaunchRecreateAndScreeningRole() {
  val instrumentation=InstrumentationRegistry.getInstrumentation()
  val context=instrumentation.targetContext
  val role=context.getSystemService(RoleManager::class.java)
  assertTrue("Phone emulator must support screening",role.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING))
  // Shell grant is an emulator integration check, not proof of the Samsung user-consent dialog.
  instrumentation.uiAutomation.executeShellCommand("cmd role add-role-holder --user 0 android.app.role.CALL_SCREENING fr.spamoff").use {
   android.os.ParcelFileDescriptor.AutoCloseInputStream(it).readBytes()
  }
  assertTrue(role.isRoleHeld(RoleManager.ROLE_CALL_SCREENING))
  ActivityScenario.launch(MainActivity::class.java).use { scenario ->
   instrumentation.waitForIdleSync()
   scenario.onActivity { assertFalse(it.isFinishing) }
   scenario.recreate()
   instrumentation.waitForIdleSync()
   scenario.onActivity { assertFalse(it.isFinishing) }
   val screen=instrumentation.uiAutomation.takeScreenshot()
   assertNotNull(screen)
   File(context.filesDir,"home-api-${Build.VERSION.SDK_INT}.png").outputStream().use {
    screen!!.compress(Bitmap.CompressFormat.PNG,100,it)
   }
  }
 }
}
