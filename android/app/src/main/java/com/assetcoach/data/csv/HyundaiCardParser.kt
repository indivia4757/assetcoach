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
 * 현대카드 CSV 파서 (UTF-8, ISO 날짜 형식).
 *
 * 양식:
 *   결제일,결제시각,가맹점,카테고리,결제금액(원),할부,취소
 *   2026-04-12,14:23:00,스타벅스 종로점,카페,5800,0,
 */
class HyundaiCardParser : CsvParser {
    override val sourceTag = "hyundai-card-v1"

    private val dateFmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).apply {
        timeZone = TimeZone.getTimeZone("Asia/Seoul")
    }

    override fun matchesHeader(firstNonBlankLine: String): Boolean =
        firstNonBlankLine.contains("결제일") && firstNonBlankLine.contains("결제시각")

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
        val rawAmount = cols[4].replace(",", "").trim().toLongOrNull() ?: return null
        val installment = cols.getOrNull(5)?.trim()?.toIntOrNull() ?: 0
        val cancelled = cols.getOrNull(6)?.trim()?.equals("취소", ignoreCase = true) == true ||
            rawAmount < 0
        val signed = if (cancelled) kotlin.math.abs(rawAmount) else -kotlin.math.abs(rawAmount)
        val merchantNorm = MerchantNormalizer.normalize(merchant)
        val cls = CategoryClassifier.classify(merchantNorm, amount = signed)
        return TransactionEntity(
            ts = ts, amount = signed, rawText = merchant, merchantNorm = merchantNorm,
            categoryId = cls.categoryId, categoryConfidence = cls.confidence,
            categorySource = cls.source, installmentMonths = installment,
            isRefund = cancelled, rawSource = sourceTag
        )
    }
}
