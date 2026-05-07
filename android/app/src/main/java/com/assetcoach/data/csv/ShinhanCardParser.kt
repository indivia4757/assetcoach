package com.assetcoach.data.csv

import com.assetcoach.data.db.entity.TransactionEntity
import com.assetcoach.domain.classifier.CategoryClassifier
import com.assetcoach.domain.classifier.MerchantNormalizer
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * 신한카드 CSV 파서.
 *
 * 양식 (헤더 1행):
 *   거래일자,승인시각,가맹점명,업종,승인금액,할부개월,승인번호,취소여부
 *   20260412,142300,스타벅스 종로점,일반음식,5800,0,12345678,N
 *
 * 인코딩: CP949 가 표준이지만 본 샘플은 UTF-8 — `charset` 파라미터로 지정.
 */
class ShinhanCardParser(
    private val charset: Charset = Charsets.UTF_8
) {

    private val dateFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.KOREA).apply {
        timeZone = TimeZone.getTimeZone("Asia/Seoul")
    }

    fun parse(input: InputStream): List<TransactionEntity> {
        val reader = BufferedReader(InputStreamReader(input, charset))
        val rows = mutableListOf<TransactionEntity>()
        var headerSeen = false
        reader.useLines { lines ->
            lines.forEach { line ->
                if (line.isBlank()) return@forEach
                if (!headerSeen) {
                    if (line.contains("거래일자") || line.contains("승인시각")) {
                        headerSeen = true
                        return@forEach
                    }
                    headerSeen = true  // 헤더 없는 형식도 있을 수 있음
                }
                parseRow(line)?.let { rows.add(it) }
            }
        }
        return rows
    }

    private fun parseRow(line: String): TransactionEntity? {
        // CSV split — 따옴표로 감싸인 필드는 쉼표 포함 가능. 단순화: 따옴표 없는 행만 처리.
        val cols = splitCsv(line)
        if (cols.size < 5) return null

        val dateRaw = cols[0].replace(".", "").replace("/", "").replace("-", "").trim()
        val timeRaw = cols[1].replace(":", "").trim()
        val merchantRaw = cols[2].trim()
        // val industry = cols.getOrNull(3) ?: ""
        val amountRaw = cols[4].trim()
        val installmentRaw = cols.getOrNull(5)?.trim() ?: "0"
        val cancelFlag = cols.getOrNull(7)?.trim() ?: "N"

        if (dateRaw.length < 8) return null

        val ts = parseTimestamp(dateRaw, timeRaw) ?: return null
        val amountAbs = amountRaw.replace(",", "").replace("\"", "").toLongOrNull() ?: return null
        val isRefund = cancelFlag.equals("Y", ignoreCase = true) || amountRaw.startsWith("-")
        val signedAmount = if (isRefund) amountAbs.let { kotlin.math.abs(it) } else -kotlin.math.abs(amountAbs)
        val installment = installmentRaw.toIntOrNull() ?: 0

        val merchantNorm = MerchantNormalizer.normalize(merchantRaw)
        val classification = CategoryClassifier.classify(merchantNorm, amount = signedAmount)

        return TransactionEntity(
            ts = ts,
            amount = signedAmount,
            rawText = merchantRaw,
            merchantNorm = merchantNorm,
            categoryId = classification.categoryId,
            categoryConfidence = classification.confidence,
            categorySource = classification.source,
            installmentMonths = installment,
            isRefund = isRefund,
            rawSource = "shinhan-card-v1"
        )
    }

    private fun parseTimestamp(date: String, time: String): Long? {
        return try {
            val padded = (date + time.padEnd(6, '0')).substring(0, 14)
            dateFormat.parse(padded)?.time
        } catch (e: Exception) {
            null
        }
    }

    private fun splitCsv(line: String): List<String> {
        val result = mutableListOf<String>()
        var cur = StringBuilder()
        var inQuote = false
        for (ch in line) {
            when {
                ch == '"' -> inQuote = !inQuote
                ch == ',' && !inQuote -> {
                    result.add(cur.toString())
                    cur = StringBuilder()
                }
                else -> cur.append(ch)
            }
        }
        result.add(cur.toString())
        return result
    }
}
