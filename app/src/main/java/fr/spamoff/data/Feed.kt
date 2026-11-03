package fr.spamoff.data

import fr.spamoff.core.Numbers
import org.json.JSONObject
import java.security.MessageDigest

data class ValidatedFeed(val version: Long, val source: String, val rules: List<Rule>)
object FeedValidator {
 const val MAX_BYTES = 8 * 1024 * 1024
 fun validate(manifest: ByteArray, body: ByteArray): ValidatedFeed {
  require(manifest.size <= 4096 && body.size in 1..MAX_BYTES) { "Taille de liste invalide" }
  val m = JSONObject(manifest.toString(Charsets.UTF_8))
  require(m.getInt("schema") == 1)
  val hash = MessageDigest.getInstance("SHA-256").digest(body).joinToString("") { "%02x".format(it) }
  require(m.getString("sha256") == hash) { "Intégrité SHA-256 incorrecte" }
  val j = JSONObject(body.toString(Charsets.UTF_8))
  require(j.getInt("schema") == 1 && j.getLong("version") == m.getLong("version"))
  require(j.getLong("version") > 0)
  val source = j.getString("source"); require(source.length in 1..300)
  val result = mutableSetOf<Rule>()
  for ((key, kind) in listOf("exact" to "exact", "prefixes" to "prefix")) {
   val a = j.getJSONArray(key); require(a.length() <= 100000)
   for (i in 0 until a.length()) {
    val value = a.getString(i)
    require(if (kind == "exact") Numbers.canonical(value) && Numbers.normalize(value) == value else Numbers.prefix(value)) { "Numéro ou préfixe invalide" }
    result += Rule(value, kind)
   }
  }
  require(result.isNotEmpty()) { "Liste vide refusée" }
  return ValidatedFeed(j.getLong("version"), source, result.toList())
 }
}
