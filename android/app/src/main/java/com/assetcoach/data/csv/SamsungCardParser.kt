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
 * 삼성카드 CSV 파서.
 *
 * 양식 (헤더 1행, CP949):
 *   이용일,이용시간,이용가맹점,업종구분,이용금액,할부,승인번호
 *   2026/04/12,14:23,스타벅스 종로점,요식업,5800,일시불,12345
 */
class SamsungCardParser(
    private val charset: Charset = Charsets.UTF_8
) {

    private val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.KOREA).apply {
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
                    if (line.contains("이용일") || line.contains("이용시간")) {
                        headerSeen = true
                        return@forEach
                    }
                    headerSeen = true
                }
                parseRow(line)?.let { rows.add(it) }
            }
        }
        return rows
    }

    private fun parseRow(line: String): TransactionEntity? {
        val cols = splitCsv(line)
        if (cols.size < 5) return null

        val date = cols[0].trim()  // 2026/04/12
        val time = cols[1].trim()  // 14:23
        val merchant = cols[2].trim()
        val amount = cols[4].trim().replace(",", "").toLongOrNull() ?: return null
        val installmentRaw = cols.getOrNull(5)?.trim() ?: "일시불"

        val timestamp = try {
            dateFormat.parse("$date $time")?.time ?: return null
        } catch (e: Exception) {
            return null
        }

        val installment = when {
            installmentRaw.contains("일시불") -> 0
            else -> Regex("\\d+").find(installmentRaw)?.value?.toIntOrNull() ?: 0
        }

        val merchantNorm = MerchantNormalizer.normalize(merchant)
        val classification = CategoryClassifier.classify(merchantNorm, amount = -amount)

        return TransactionEntity(
            ts = timestamp,
            amount = -amount,        // 결제 = 음수
            rawText = merchant,
            merchantNorm = merchantNorm,
            categoryId = classification.categoryId,
            categoryConfidence = classification.confidence,
            categorySource = classification.source,
            installmentMonths = installment,
            isRefund = false,
            rawSource = "samsung-card-v1"
        )
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
