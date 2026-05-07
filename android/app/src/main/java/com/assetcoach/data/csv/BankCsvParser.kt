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
 * 은행 거래내역 공통 파서 — 신한은행 / KB은행 / 우리은행 양식이 거의 동일.
 *
 * 처리 규칙:
 *  - 헤더 위 메타정보 행 (조회기간 / 계좌번호 / 빈 행) 자동 스킵
 *  - "거래일/거래일자" 키워드로 헤더 식별
 *  - "합계" 행 스킵
 *  - 출금 / 입금 두 컬럼 — 둘 중 하나만 채워짐
 *  - 적요 정규화: "체크 X" → "X" (체크 = 결제 수단 prefix)
 */
class BankCsvParser(
    val bankName: String  // "신한은행", "KB은행", "우리은행"
) : CsvParser {

    override val sourceTag: String = when (bankName) {
        "신한은행" -> "shinhan-bank-v1"
        "KB은행", "KB국민은행" -> "kb-bank-v1"
        "우리은행" -> "woori-bank-v1"
        else -> "bank-v1"
    }

    private val dateFormats = listOf(
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA),
        SimpleDateFormat("yyyyMMdd HHmmss", Locale.KOREA),
        SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.KOREA)
    ).onEach { it.timeZone = TimeZone.getTimeZone("Asia/Seoul") }

    override fun matchesHeader(firstNonBlankLine: String): Boolean {
        val l = firstNonBlankLine
        return (l.contains("거래일") || l.contains("거래일자")) &&
            (l.contains("출금") || l.contains("찾으신") || l.contains("입금") || l.contains("맡기신"))
    }

    override fun parse(input: InputStream, charset: Charset): List<TransactionEntity> {
        val reader = BufferedReader(InputStreamReader(input, charset))
        val rows = mutableListOf<TransactionEntity>()
        val lines = reader.readLines()

        // 헤더 행 찾기
        val headerIdx = lines.indexOfFirst { matchesHeader(it) }
        if (headerIdx < 0) return emptyList()

        val header = splitCsvLine(lines[headerIdx]).map { it.trim() }
        val dateIdx = header.indexOfFirst { it.contains("거래일") }
        val timeIdx = header.indexOfFirst { it.contains("거래시") || it.contains("시각") || it.contains("시간") }
        val descIdx = header.indexOfFirst { it.contains("적요") }
        val outIdx = header.indexOfFirst { it.contains("출금") || it.contains("찾으신") }
        val inIdx = header.indexOfFirst { it.contains("입금") || it.contains("맡기신") }

        for (i in (headerIdx + 1) until lines.size) {
            val line = lines[i]
            if (line.isBlank()) continue
            if (line.startsWith("합계") || line.startsWith("총")) continue

            val cols = splitCsvLine(line).map { it.trim() }
            if (cols.size <= maxOf(dateIdx, descIdx, outIdx, inIdx)) continue

            val date = cols.getOrNull(dateIdx)?.replace(".", "-") ?: continue
            val time = cols.getOrNull(timeIdx) ?: "00:00:00"
            val ts = parseTimestamp(date, time) ?: continue
            val desc = cols.getOrNull(descIdx) ?: "기타"
            val out = cols.getOrNull(outIdx)?.replace(",", "")?.toLongOrNull() ?: 0L
            val income = cols.getOrNull(inIdx)?.replace(",", "")?.toLongOrNull() ?: 0L

            val signed = when {
                out > 0 -> -out
                income > 0 -> income
                else -> continue
            }

            val raw = stripPaymentPrefix(desc)
            val merchantNorm = MerchantNormalizer.normalize(raw)
            val cls = CategoryClassifier.classify(merchantNorm, amount = signed)

            rows.add(
                TransactionEntity(
                    ts = ts, amount = signed,
                    rawText = raw, merchantNorm = merchantNorm,
                    categoryId = cls.categoryId,
                    categoryConfidence = cls.confidence,
                    categorySource = cls.source,
                    rawSource = sourceTag
                )
            )
        }
        return rows
    }

    private fun parseTimestamp(date: String, time: String): Long? {
        val combined = "$date $time"
        for (fmt in dateFormats) {
            try {
                fmt.parse(combined)?.time?.let { return it }
            } catch (e: Exception) { /* try next */ }
        }
        return null
    }

    private fun stripPaymentPrefix(s: String): String {
        val prefixes = listOf("체크", "신용", "BC카드", "자동이체", "급여")
        var x = s.trim()
        for (p in prefixes) {
            if (x.startsWith("$p ")) x = x.substring(p.length + 1).trim()
        }
        return x
    }
}
