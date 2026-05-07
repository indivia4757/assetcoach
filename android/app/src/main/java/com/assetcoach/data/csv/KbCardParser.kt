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
 * KB국민카드 CSV 파서 (CP949, 점 구분 날짜).
 *
 * 양식:
 *   사용일,승인시간,사용가맹점명,업종명,이용금액,할부개월수,비고
 *   2026.04.12,14:23,스타벅스 종로,휴게음식점,5800,0,
 *   2026.04.20,10:30,쿠팡,통신판매,"42,000",3,
 */
class KbCardParser : CsvParser {
    override val sourceTag = "kb-card-v1"

    private val dateFmt = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.KOREA).apply {
        timeZone = TimeZone.getTimeZone("Asia/Seoul")
    }

    override fun matchesHeader(firstNonBlankLine: String): Boolean =
        firstNonBlankLine.contains("사용일") && firstNonBlankLine.contains("승인시간")

    override fun parse(input: InputStream, charset: Charset): List<TransactionEntity> {
        val reader = BufferedReader(InputStreamReader(input, charset))
        val rows = mutableListOf<TransactionEntity>()
        var headerSeen = false
        reader.useLines { lines ->
            lines.forEach { line ->
                if (line.isBlank()) return@forEach
                if (!headerSeen) { headerSeen = true; return@forEach }
                parseRow(line)?.let { rows.add(it) }
            }
        }
        return rows
    }

    private fun parseRow(line: String): TransactionEntity? {
        val cols = splitCsvLine(line)
        if (cols.size < 5) return null
        val ts = try { dateFmt.parse("${cols[0].trim()} ${cols[1].trim()}")?.time ?: return null }
        catch (e: Exception) { return null }
        val merchant = cols[2].trim()
        val rawAmount = cols[4].replace(",", "").replace("\"", "").trim().toLongOrNull() ?: return null
        val installment = cols.getOrNull(5)?.trim()?.toIntOrNull() ?: 0
        val signed = -kotlin.math.abs(rawAmount)
        val merchantNorm = MerchantNormalizer.normalize(merchant)
        val cls = CategoryClassifier.classify(merchantNorm, amount = signed)
        return TransactionEntity(
            ts = ts, amount = signed, rawText = merchant, merchantNorm = merchantNorm,
            categoryId = cls.categoryId, categoryConfidence = cls.confidence,
            categorySource = cls.source, installmentMonths = installment,
            rawSource = sourceTag
        )
    }
}
