package com.assetcoach.ai

/**
 * AI 응답·사용자 입력에 대한 안전 가드.
 * 출처: AssetCoach_handoff_v2.md §법적 안전 기준 + tech_arch.md §3.4 Safety Guard
 *
 * 4 가지 금기:
 *   1. 종목·티커 추천
 *   2. 수익률 예측
 *   3. 특정 상품·보험 권유
 *   4. 의료·세무 단정
 */
object SafetyGuard {

    enum class Verdict { Pass, Block, Sanitize }

    data class Outcome(
        val verdict: Verdict,
        val reason: String? = null,
        val sanitized: String? = null
    )

    private val blockKeywords = listOf(
        // 종목·티커
        "종목 추천", "주식 추천", "사면 좋", "사면 됨", "매수 추천", "매도 추천",
        // 수익률 예측
        "수익률", "오를 거", "내릴 거", "오를 가능성",
        // 의료 단정
        "이 약을", "치료하세요", "병원 가지 마",
        // 세무 단정
        "환급 받으세요", "절세 가능", "신고하지 마"
    )

    private val sanitizePatterns = listOf(
        Regex("연\\s*\\d+%\\s*(예상|기대|보장)") to "[수익률 예측 표현 검열]",
        Regex("(테슬라|애플|삼성전자|AAPL|TSLA|005930)\\s*(사|매수|투자)") to "[종목 추천 표현 검열]"
    )

    fun checkUserInput(text: String): Outcome {
        // 사용자 질문은 대부분 통과 — Block 은 거의 없고, AI 응답에 적용
        return Outcome(Verdict.Pass)
    }

    fun checkAiResponse(text: String): Outcome {
        // 1. 명백한 금기 키워드 → Block
        val matched = blockKeywords.firstOrNull { text.contains(it, ignoreCase = true) }
        if (matched != null) {
            return Outcome(Verdict.Block, reason = "금기 표현: $matched")
        }
        // 2. 패턴 검열 → Sanitize
        var sanitized = text
        var changed = false
        for ((rx, replacement) in sanitizePatterns) {
            if (rx.containsMatchIn(sanitized)) {
                sanitized = sanitized.replace(rx, replacement)
                changed = true
            }
        }
        return if (changed) Outcome(Verdict.Sanitize, sanitized = sanitized)
        else Outcome(Verdict.Pass)
    }
}
