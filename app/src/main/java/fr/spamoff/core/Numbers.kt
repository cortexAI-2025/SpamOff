package fr.spamoff.core

import com.google.i18n.phonenumbers.PhoneNumberUtil

object Numbers {
 private val util = PhoneNumberUtil.getInstance()
 fun normalize(raw: String): String? = runCatching {
  if (raw.length > 80 || raw.any { it.isLetter() || it in "*#;," }) return null
  val value = if (raw.trim().startsWith("00")) "+" + raw.trim().drop(2) else raw.trim()
  val parsed = util.parse(value, "FR")
  if (parsed.hasExtension() || !util.isValidNumber(parsed)) null
  else util.format(parsed, PhoneNumberUtil.PhoneNumberFormat.E164)
 }.getOrNull()
 fun canonical(value: String) = Regex("\\+[1-9][0-9]{6,14}").matches(value)
 fun prefix(value: String) = Regex("\\+[1-9][0-9]{2,10}").matches(value)
}

enum class Verdict { ALLOW, PERSONAL, AUTOMATED, COMMUNITY }
object DecisionEngine {
 fun decide(number: String?, enabled: Boolean, automated: Boolean,
  allowed: Boolean, personal: Boolean, exact: Boolean, prefix: Boolean): Verdict = when {
  !enabled || number == null || allowed -> Verdict.ALLOW
  personal -> Verdict.PERSONAL
  exact -> Verdict.COMMUNITY
  automated && prefix -> Verdict.AUTOMATED
  else -> Verdict.ALLOW
 }
}
