package com.assetcoach.data.csv

import com.assetcoach.data.db.entity.TransactionEntity
import java.io.InputStream
import java.nio.charset.Charset

/**
 * CSV 파서 공통 인터페이스 — 카드사·은행 양식별 구현체.
 * 출처: AssetCoach_csv_format_specs.md §정규화 모델.
 */
interface CsvParser {
    /** 디버그용 양식 식별자 (raw_source 컬럼에 기록) */
    val sourceTag: String

    /** 헤더 첫 줄로 양식 일치 여부 추정. 공통 디스패처가 사용. */
    fun matchesHeader(firstNonBlankLine: String): Boolean

    fun parse(input: InputStream, charset: Charset = Charsets.UTF_8): List<TransactionEntity>
}

/**
 * 단순 CSV split — 따옴표 안 쉼표 보존.
 */
internal fun splitCsvLine(line: String): List<String> {
    val out = mutableListOf<String>()
    var cur = StringBuilder()
    var inQuote = false
    for (ch in line) {
        when {
            ch == '"' -> inQuote = !inQuote
            ch == ',' && !inQuote -> {
                out.add(cur.toString())
                cur = StringBuilder()
            }
            else -> cur.append(ch)
        }
    }
    out.add(cur.toString())
    return out
}
