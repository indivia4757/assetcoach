package com.assetcoach.data.csv

import com.assetcoach.data.db.entity.TransactionEntity
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset

/**
 * CSV 인코딩 자동 감지 + 양식 자동 라우팅 + 파싱.
 *
 * 입력: byte[] (또는 InputStream)
 * 출력: 파싱된 거래 리스트 + 양식 정보
 */
class CsvImporter {

    private val parsers: List<CsvParser> = listOf(
        // Card parsers
        ShinhanCardParserAdapter,
        SamsungCardParserAdapter,
        HyundaiCardParser(),
        KbCardParser(),
        // Bank parsers — 한 인스턴스가 3종 양식 처리
        BankCsvParser("신한은행"),
        BankCsvParser("KB은행"),
        BankCsvParser("우리은행")
    )

    data class ImportResult(
        val transactions: List<TransactionEntity>,
        val sourceTag: String,
        val charset: String
    )

    fun import(bytes: ByteArray): ImportResult {
        val charset = detectCharset(bytes)
        val firstLine = readFirstNonBlankLine(bytes, charset)

        val matched = parsers.firstOrNull { it.matchesHeader(firstLine) }
            ?: throw IllegalArgumentException(
                "지원하지 않는 양식이에요. 카드/은행 ${parsers.size} 종 양식만 지원합니다."
            )

        val txs = matched.parse(ByteArrayInputStream(bytes), charset)
        return ImportResult(
            transactions = txs,
            sourceTag = matched.sourceTag,
            charset = charset.name()
        )
    }

    private fun detectCharset(bytes: ByteArray): Charset {
        // 1. UTF-8 BOM
        if (bytes.size >= 3 &&
            bytes[0] == 0xEF.toByte() &&
            bytes[1] == 0xBB.toByte() &&
            bytes[2] == 0xBF.toByte()
        ) return Charsets.UTF_8

        // 2. UTF-8 vs CP949 — 처음 2KB 디코딩 시도
        val sample = bytes.copyOf(minOf(2048, bytes.size))
        return if (looksLikeUtf8(sample)) Charsets.UTF_8 else charset("CP949")
    }

    private fun looksLikeUtf8(sample: ByteArray): Boolean {
        try {
            val decoded = String(sample, Charsets.UTF_8)
            // 한글 또는 ASCII 비율
            val koreanCount = decoded.count { it in '가'..'힣' || it in 'A'..'Z' || it in 'a'..'z' }
            val replacementCount = decoded.count { it == '�' }
            return replacementCount == 0 && koreanCount > 5
        } catch (e: Exception) {
            return false
        }
    }

    private fun readFirstNonBlankLine(bytes: ByteArray, charset: Charset): String {
        BufferedReader(InputStreamReader(ByteArrayInputStream(bytes), charset)).use { reader ->
            while (true) {
                val line = reader.readLine() ?: return ""
                if (line.isNotBlank()) return line
            }
            @Suppress("UNREACHABLE_CODE")
            return ""
        }
    }
}

// ─────────────────────────────────────────────────────────
// Adapter — 기존 파서 (Shinhan/Samsung) 를 CsvParser 인터페이스로 래핑
// ─────────────────────────────────────────────────────────

private object ShinhanCardParserAdapter : CsvParser {
    override val sourceTag = "shinhan-card-v1"
    override fun matchesHeader(firstNonBlankLine: String): Boolean =
        firstNonBlankLine.contains("거래일자") && firstNonBlankLine.contains("승인시각")

    override fun parse(input: InputStream, charset: Charset): List<TransactionEntity> =
        ShinhanCardParser(charset).parse(input)
}

private object SamsungCardParserAdapter : CsvParser {
    override val sourceTag = "samsung-card-v1"
    override fun matchesHeader(firstNonBlankLine: String): Boolean =
        firstNonBlankLine.contains("이용일") && firstNonBlankLine.contains("이용시간")

    override fun parse(input: InputStream, charset: Charset): List<TransactionEntity> =
        SamsungCardParser(charset).parse(input)
}
