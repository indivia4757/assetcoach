package com.assetcoach.data.sms

import com.assetcoach.data.db.entity.TransactionEntity
import com.assetcoach.domain.classifier.CategoryClassifier
import com.assetcoach.domain.classifier.MerchantNormalizer
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/**
 * SMS 결제 알림 파서. 카드사·은행 발신 화이트리스트 + 정규식 매칭.
 * 매칭 실패 시 LLM 폴백 (Phase 3+ A4 호출) 호출 가능.
 *
 * 출처: AssetCoach_csv_format_specs.md §SMS 알림 골든.
 */
object SmsParser {

    private val cardIssuerWhitelist = listOf(
        "신한카드", "삼성카드", "현대카드", "KB카드", "KB국민카드",
        "신한은행", "KB은행", "KB국민은행", "우리은행"
    )

    /**
     * 정규식 1차 시도 — 표준 카드사 알림 양식.
     * 예: "[Web발신]\n신한카드 승인 김XX님\n5,800원 04/22 14:23\n스타벅스 종로점"
     */
    private val standardPattern = Regex(
        "(?<issuer>${cardIssuerWhitelist.joinToString("|")})\\s*승인.*?\\s*(?<amount>[\\d,]+)원\\s+(?<month>\\d{2})/(?<day>\\d{2})\\s+(?<hour>\\d{2}):(?<minute>\\d{2})\\s+(?<merchant>.+?)\$",
        RegexOption.DOT_MATCHES_ALL
    )

    fun isFromKnownIssuer(sender: String): Boolean =
        cardIssuerWhitelist.any { sender.contains(it) }

    /**
     * SMS 본문 → Transaction 또는 null (폴백 필요).
     */
    fun parse(body: String, currentYear: Int = 2026): TransactionEntity? {
        val match = standardPattern.find(body) ?: return null

        val issuer = match.groups["issuer"]?.value ?: return null
        val amountRaw = match.groups["amount"]?.value?.replace(",", "")?.toLongOrNull() ?: return null
        val month = match.groups["month"]?.value?.toIntOrNull() ?: return null
        val day = match.groups["day"]?.value?.toIntOrNull() ?: return null
        val hour = match.groups["hour"]?.value?.toIntOrNull() ?: return null
        val minute = match.groups["minute"]?.value?.toIntOrNull() ?: return null
        val merchantRaw = match.groups["merchant"]?.value?.trim() ?: return null

        val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul")).apply {
            set(currentYear, month - 1, day, hour, minute, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val ts = cal.timeInMillis

        val merchantNorm = MerchantNormalizer.normalize(merchantRaw)
        val classification = CategoryClassifier.classify(merchantNorm, amount = -amountRaw)

        return TransactionEntity(
            ts = ts,
            amount = -amountRaw,
            rawText = merchantRaw,
            merchantNorm = merchantNorm,
            categoryId = classification.categoryId,
            categoryConfidence = classification.confidence,
            categorySource = "rule",
            rawSource = "sms-${issuer.lowercase()}"
        )
    }
}
