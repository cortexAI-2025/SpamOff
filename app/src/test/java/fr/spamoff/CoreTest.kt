package fr.spamoff

import fr.spamoff.core.*
import fr.spamoff.data.*
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import java.security.MessageDigest

class CoreTest {
 @Test fun frenchFormats() { for (n in listOf("06 12 34 56 78", "+33 6 12 34 56 78", "0033612345678")) assertEquals("+33612345678",Numbers.normalize(n)) }
 @Test fun international() { assertEquals("+14155552671",Numbers.normalize("+1 415 555 2671")); assertEquals("+212612345678",Numbers.normalize("+212612345678")) }
 @Test fun rejectServiceCodes() { for (n in listOf("112","15","*123#","","anonymous","+33","0612345678;123","06CALLME","0612345678 ext 3")) assertNull(n,Numbers.normalize(n)) }
 @Test fun rejectLongInputs() { assertNull(Numbers.normalize("1".repeat(81))) }
 @Test fun canonical() { assertTrue(Numbers.canonical("+33612345678")); assertFalse(Numbers.canonical("0612345678")); assertFalse(Numbers.canonical("+330612345678 ")) }
 @Test fun whitelistWins() { assertEquals(Verdict.ALLOW,DecisionEngine.decide("+33162123456",true,true,true,true,true,true)) }
 @Test fun personalBlocks() { assertEquals(Verdict.PERSONAL,DecisionEngine.decide("+33612345678",true,false,false,true,false,false)) }
 @Test fun disabledAllows() { assertEquals(Verdict.ALLOW,DecisionEngine.decide("+33612345678",false,true,false,true,true,true)) }
 @Test fun nullAllows() { assertEquals(Verdict.ALLOW,DecisionEngine.decide(null,true,true,false,true,true,true)) }
 @Test fun legitimateAllows() { assertEquals(Verdict.ALLOW,DecisionEngine.decide("+33612345678",true,true,false,false,false,false)) }
 @Test fun rangesOptIn() { assertEquals(Verdict.ALLOW,DecisionEngine.decide("+33162123456",true,false,false,false,false,true)); assertEquals(Verdict.AUTOMATED,DecisionEngine.decide("+33162123456",true,true,false,false,false,true)) }
 @Test fun exactBlocks() { assertEquals(Verdict.COMMUNITY,DecisionEngine.decide("+33612345678",true,false,false,false,true,false)) }
 private fun body(version: Long=1, exact: String="+33612345678") = JSONObject().put("schema",1).put("version",version).put("source","Test").put("exact",org.json.JSONArray(listOf(exact,exact))).put("prefixes",org.json.JSONArray(listOf("+33162"))).toString().toByteArray()
 private fun manifest(body: ByteArray, version: Long=1): ByteArray {
  val hash = MessageDigest.getInstance("SHA-256").digest(body).joinToString("") { "%02x".format(it) }
  return JSONObject().put("schema",1).put("version",version).put("sha256",hash).toString().toByteArray()
 }
 @Test fun validAndDeduplicated() { val b=body(); val f=FeedValidator.validate(manifest(b),b); assertEquals(2,f.rules.size) }
 @Test(expected=IllegalArgumentException::class) fun corrupted() { val b=body(); FeedValidator.validate(manifest(b),b+32) }
 @Test(expected=IllegalArgumentException::class) fun incomplete() { val b=body(); FeedValidator.validate(manifest(b),b.copyOf(b.size/2)) }
 @Test(expected=IllegalArgumentException::class) fun inconsistentVersion() { val b=body(2); FeedValidator.validate(manifest(b),b) }
 @Test(expected=IllegalArgumentException::class) fun invalidNumber() { val b=body(exact="0612345678"); FeedValidator.validate(manifest(b),b) }
 @Test(expected=IllegalArgumentException::class) fun oversize() { FeedValidator.validate(ByteArray(0),ByteArray(FeedValidator.MAX_BYTES+1)) }
 @Test(expected=IllegalArgumentException::class) fun broadPrefix() { val b=body().toString(Charsets.UTF_8).replace("+33162","+3").toByteArray(); FeedValidator.validate(manifest(b),b) }
 @Test fun shippedAssetValid() {
  val root=java.io.File("src/main/assets")
  val f=FeedValidator.validate(java.io.File(root,"manifest.json").readBytes(),java.io.File(root,"rules.json").readBytes())
  assertEquals(22,f.rules.size); assertTrue(f.rules.all { it.kind=="prefix" })
 }
}
