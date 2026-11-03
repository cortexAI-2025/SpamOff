package fr.spamoff.data

import android.content.Context
import androidx.room.withTransaction
import fr.spamoff.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Streaming
import java.util.concurrent.TimeUnit

interface FeedApi {
 @Streaming @GET("manifest.json") suspend fun manifest(): ResponseBody
 @Streaming @GET("rules.json") suspend fun rules(): ResponseBody
}
class SpamRepository(private val context: Context, val db: SpamDatabase, val settings: SettingsRepository) {
 val dao = db.dao()
 private val api by lazy {
  Retrofit.Builder().baseUrl("https://raw.githubusercontent.com/cortexAI-2025/SpamOff/main/data/")
   .client(OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).readTimeout(20, TimeUnit.SECONDS)
    .callTimeout(30, TimeUnit.SECONDS).followRedirects(false).followSslRedirects(false).build())
   .build().create(FeedApi::class.java)
 }
 suspend fun seed() = withContext(Dispatchers.IO) {
  if (dao.metadata() == null) {
   val manifest = context.assets.open("manifest.json").use { it.readBytes() }
   val body = context.assets.open("rules.json").use { it.readBytes() }
   val feed = FeedValidator.validate(manifest, body)
   db.withTransaction { if (dao.metadata() == null) { dao.rules(feed.rules); dao.putMetadata(Metadata(version=feed.version, updated=0, source=feed.source)) } }
  }
 }
 suspend fun decide(raw: String?): Verdict {
  val s = settings.state.first()
  if (!s.enabled) return Verdict.ALLOW
  val number = raw?.let(Numbers::normalize) ?: return Verdict.ALLOW
  val p = dao.person(number)
  if (p?.allowed == true) return Verdict.ALLOW
  if (p != null) return Verdict.PERSONAL
  seed()
  return DecisionEngine.decide(number, s.enabled, s.automated, false, false, dao.exact(number),
   s.automated && dao.prefix((3..number.length).map { number.take(it) }))
 }
 suspend fun save(raw: String, allow: Boolean) {
  val n = requireNotNull(Numbers.normalize(raw)) { "Numéro invalide. Utilisez +indicatif pour l’étranger." }
  dao.put(Personal(n, allow))
 }
 suspend fun report(raw: String, category: String, comment: String, block: Boolean) {
  val n = requireNotNull(Numbers.normalize(raw)) { "Numéro invalide" }
  require(category in listOf("Démarchage", "Fraude", "Robot vocal", "Spam", "Autre"))
  require(comment.length <= 500)
  db.withTransaction { dao.report(LocalReport(n, category, comment)); if (block) dao.put(Personal(n, false)) }
 }
 suspend fun install(feed: ValidatedFeed) = db.withTransaction {
  val old = dao.metadata()
  require(old == null || feed.version >= old.version) { "Version de liste antérieure refusée" }
  dao.clearRules(); dao.rules(feed.rules)
  dao.putMetadata(Metadata(version=feed.version, updated=System.currentTimeMillis(), source=feed.source))
 }
 suspend fun update() = withContext(Dispatchers.IO) {
  val manifest = api.manifest().bounded(4096)
  val body = api.rules().bounded(FeedValidator.MAX_BYTES)
  install(FeedValidator.validate(manifest, body))
 }
 private fun ResponseBody.bounded(limit: Int): ByteArray = use {
  require(contentLength() <= limit)
  byteStream().use { stream ->
   val out = java.io.ByteArrayOutputStream(); val buffer = ByteArray(8192)
   while (true) { val n = stream.read(buffer); if(n < 0) break; require(out.size()+n <= limit); out.write(buffer,0,n) }
   out.toByteArray()
  }
 }
}
