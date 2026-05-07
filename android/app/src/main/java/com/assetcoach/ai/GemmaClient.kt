package com.assetcoach.ai

import kotlinx.coroutines.flow.Flow

/**
 * Gemma 4 추론 클라이언트의 공통 인터페이스.
 *
 * 두 가지 구현:
 *  - GemmaMockClient: 모델 파일 없을 때 키워드 기반 시뮬레이션 (Phase 3 데모)
 *  - GemmaRealClient: MediaPipe LLM Inference + Gemma 4 INT4 (Phase 3 실 통합 — 모델 가중치 다운로드 후)
 *
 * 인터페이스가 같으니 ViewModel 은 어떤 구현체인지 모르고 사용 가능.
 */
interface GemmaClient {

    /** 단일 사용자 메시지 → 토큰 스트리밍 응답 */
    fun generate(
        userMessage: String,
        systemPrompt: String,
        history: List<HistoryTurn>,
        context: GenerationContext
    ): Flow<TokenChunk>

    fun isReady(): Boolean
}

data class HistoryTurn(val role: String, val content: String)

/** Gemma 에 주입할 거래 컨텍스트 — Citation 링킹용 */
data class GenerationContext(
    val recentTransactions: List<TxBrief>,
    val monthSummary: MonthSummary?,
    val segmentBenchmark: Map<String, Long>?     // 카테고리 → 평균값
)

data class TxBrief(
    val id: Long,
    val date: String,
    val merchant: String,
    val amount: Long,
    val category: String
)

data class MonthSummary(
    val totalSpend: Long,
    val previousMonthSpend: Long?,
    val topCategories: List<Pair<String, Long>>
)

/** 응답 토큰 (단어 또는 문자 단위) + 메타 */
data class TokenChunk(
    val text: String,
    val isFinal: Boolean = false,
    val citationTxIds: List<Long> = emptyList()
)
