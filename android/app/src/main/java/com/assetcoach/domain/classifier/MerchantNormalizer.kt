package com.assetcoach.domain.classifier

/**
 * 가맹점 원문 → 정규화된 가맹점명.
 * 디자인 시스템 §6.1 가맹점명 정규화 룰 6 종 적용.
 */
object MerchantNormalizer {

    private val branchSuffixes = listOf(
        "점", "지점", "본점", "직영점", "강남", "종로", "광화문",
        "강남역", "신촌", "홍대", "이태원", "용산", "여의도",
        "가산", "성수", "동대문", "마포", "송파", "잠실"
    )

    private val paymentPrefixes = listOf("체크", "신용", "BC", "BC카드", "법인")

    /**
     * "BC카드 스타벅스 종로점 SK B" → "스타벅스"
     */
    fun normalize(raw: String): String {
        var s = raw.trim()
        // 1. payment prefix 제거
        for (prefix in paymentPrefixes) {
            if (s.startsWith(prefix + " ")) s = s.substring(prefix.length + 1).trim()
        }
        // 2. 공백 정규화
        s = s.replace(Regex("\\s+"), " ")
        // 3. 지점명 끝 trim — "X점", "X지점", "X 강남점" 등
        // 단순히 마지막 단어가 지점명이면 잘라냄
        val tokens = s.split(" ").toMutableList()
        while (tokens.size > 1) {
            val last = tokens.last()
            val isBranch = last.endsWith("점") ||
                last.endsWith("지점") ||
                branchSuffixes.any { last.startsWith(it) || last.contains(it) }
            if (isBranch) tokens.removeAt(tokens.lastIndex) else break
        }
        return tokens.joinToString(" ")
    }
}
