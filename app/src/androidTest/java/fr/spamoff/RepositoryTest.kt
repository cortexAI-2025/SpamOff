package fr.spamoff

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import fr.spamoff.core.Verdict
import fr.spamoff.data.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RepositoryTest {
 private lateinit var db: SpamDatabase
 private lateinit var repo: SpamRepository
 private lateinit var settings: SettingsRepository
 private val context: Context = ApplicationProvider.getApplicationContext()
 @Before fun before() = runBlocking {
  db=Room.inMemoryDatabaseBuilder(context,SpamDatabase::class.java).build()
  settings=SettingsRepository(context); settings.setEnabled(true); settings.setAutomated(true)
  repo=SpamRepository(context,db,settings); repo.seed()
 }
 @After fun after() { db.close() }
 @Test fun offlinePrefixAndWhitelist() = runBlocking {
  assertEquals(Verdict.AUTOMATED,repo.decide("0162123456"))
  repo.save("0162123456",true)
  assertEquals(Verdict.ALLOW,repo.decide("+33162123456"))
 }
 @Test fun personalAndDisabled() = runBlocking {
  repo.save("0612345678",false)
  assertEquals(Verdict.PERSONAL,repo.decide("0612345678"))
  settings.setEnabled(false)
  assertEquals(Verdict.ALLOW,repo.decide("0612345678"))
 }
 @Test fun validUpdateKeepsPersonalRules() = runBlocking {
  repo.save("0612345678",true)
  repo.install(ValidatedFeed(2026110302,"Test",listOf(Rule("+33162","prefix"))))
  assertEquals(true,repo.dao.person("+33612345678")?.allowed)
  assertEquals(2026110302L,repo.dao.metadata()?.version)
 }
 @Test fun failedTransactionRetainsRules() = runBlocking {
  val initial=repo.dao.metadata()
  try { repo.install(ValidatedFeed(1,"Old",emptyList())); fail("Rollback must be rejected") } catch(_:IllegalArgumentException) { }
  assertEquals(initial,repo.dao.metadata())
  assertEquals(Verdict.AUTOMATED,repo.decide("0162123456"))
 }
 @Test fun corruptionNeverReachesInstall() = runBlocking {
  val initial=repo.dao.metadata()
  try { val bad=FeedValidator.validate(context.assets.open("manifest.json").readBytes(),byteArrayOf(1,2)); repo.install(bad); fail() } catch(_:Exception) { }
  assertEquals(initial,repo.dao.metadata()); assertEquals(Verdict.AUTOMATED,repo.decide("0162123456"))
 }
 @Test fun noUnknownAndNoLegitimateBlock() = runBlocking {
  assertEquals(Verdict.ALLOW,repo.decide(null)); assertEquals(Verdict.ALLOW,repo.decide("0612345678"))
 }
 @Test fun rangeOptOut() = runBlocking { settings.setAutomated(false); assertEquals(Verdict.ALLOW,repo.decide("0162123456")) }
 @Test fun reportLocalBlock() = runBlocking { repo.report("0612345678","Fraude","test",true); assertEquals(Verdict.PERSONAL,repo.decide("0612345678")) }
 @Test fun durableAcrossDatabaseReopen() = runBlocking {
  val name="persistence-test.db"; context.deleteDatabase(name)
  var disk=Room.databaseBuilder(context,SpamDatabase::class.java,name).build()
  disk.dao().put(Personal("+33612345678",false)); disk.close()
  disk=Room.databaseBuilder(context,SpamDatabase::class.java,name).build()
  assertEquals(false,disk.dao().person("+33612345678")?.allowed); disk.close(); context.deleteDatabase(name)
 }
 @Test fun counterDoesNotStoreCallNumbers() = runBlocking { repo.dao.increment(); repo.dao.increment(); assertEquals(0,repo.dao.people().first().size) }
}
