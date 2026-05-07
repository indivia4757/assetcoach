package com.assetcoach.ai

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Phase 3 데모 클라이언트 — 실 모델 가중치 없이 작동.
 *
 * 사용자 메시지의 키워드 + GenerationContext (실 거래 DB) 를 결합해
 * "Gemma 4 가 응답하는 것 처럼 보이는" 응답을 생성한다.
 *
 * 실제 GemmaRealClient 가 연결되면 즉시 교체 가능 (인터페이스 동일).
 *
 * 응답 정책:
 *  - 항상 거래 데이터 인용 (Citation block)
 *  - SafetyGuard 통과 표현만 사용
 *  - 한 글자씩 ~30ms 간격 스트리밍 (실제 Gemma TPS ≈ 30 모방)
 */
class GemmaMockClient : GemmaClient {

    override fun isReady(): Boolean = true

    override fun generate(
        userMessage: String,
        systemPrompt: String,
        history: List<HistoryTurn>,
        context: GenerationContext
    ): Flow<TokenChunk> = flow {
        val msg = userMessage.lowercase()

        // SafetyGuard — 명백한 금기성 질문에 대한 거절
        if (containsBlockedQuery(msg)) {
            streamWords(refusalResponse(), emptyList(), this)
            return@flow
        }

        // 키워드 분기
        when {
            msg.contains("왜") && (msg.contains("많이") || msg.contains("썼")) -> {
                respondToWhySpentMore(context, this)
            }
            msg.contains("스타벅스") || msg.contains("카페") -> {
                respondAboutCafe(context, this)
            }
            msg.contains("외식") -> respondAboutCategory(context, "외식", this)
            msg.contains("식비") -> respondAboutCategory(context, "식비", this)
            msg.contains("구독") || msg.contains("netflix") || msg.contains("넷플") -> {
                respondAboutSubscriptions(context, this)
            }
            msg.contains("중복") -> respondAboutDuplicates(context, this)
            msg.contains("이번 달") || msg.contains("총") -> respondMonthSummary(context, this)
            msg.contains("주식") || msg.contains("종목") || msg.contains("투자") -> {
                streamWords(refusalResponse(), emptyList(), this)
            }
            msg.contains("안녕") || msg.contains("hello") -> {
                streamWords("안녕하세요. 오늘은 어떤 부분을 함께 볼까요?", emptyList(), this)
            }
            else -> respondGeneric(context, this)
        }
    }

    // ─────────────────────────────────────────────────────────
    // Response builders
    // ─────────────────────────────────────────────────────────

    private suspend fun respondMonthSummary(
        ctx: GenerationContext,
        collector: kotlinx.coroutines.flow.FlowCollector<TokenChunk>
    ) {
        val total = ctx.monthSummary?.totalSpend ?: 0L
        val txt = "이번 달 총 지출은 ${formatWon(-total)}예요. 가장 큰 카테고리는 " +
            (ctx.monthSummary?.topCategories?.firstOrNull()?.first ?: "기타") + " 였어요."
        streamWords(txt, ctx.recentTransactions.take(3).map { it.id }, collector)
    }

    private suspend fun respondToWhySpentMore(
        ctx: GenerationContext,
        collector: kotlinx.coroutines.flow.FlowCollector<TokenChunk>
    ) {
        val sum = ctx.monthSummary
        val total = sum?.totalSpend ?: 0L
        val largest = ctx.recentTransactions.minByOrNull { it.amount }
        val intro = if (largest != null) {
            "이번 달은 사실 총 ${formatWon(-total)} 쓰셨어요. 그 중 가장 큰 한 건이 " +
                "${largest.merchant} ${formatWon(-largest.amount)} 였어요."
        } else {
            "이번 달 지출 ${formatWon(-total)} 중 평소보다 큰 건이 보이지는 않아요."
        }
        val followup = "이게 일회성인지 패턴인지 함께 볼까요?"
        streamWords(intro + " " + followup, listOfNotNull(largest?.id), collector)
    }

    private suspend fun respondAboutCafe(
        ctx: GenerationContext,
        collector: kotlinx.coroutines.flow.FlowCollector<TokenChunk>
    ) {
        val cafes = ctx.recentTransactions.filter {
            it.merchant.contains("스타벅스") || it.category == "카페"
        }
        if (cafes.isEmpty()) {
            streamWords("이번 달 카페 결제는 아직 잡히지 않아요.", emptyList(), collector)
            return
        }
        val total = cafes.sumOf { -it.amount }
        val text = "${cafes.size}건 카페 결제, 합계 ${formatWon(total)}예요. " +
            "가장 자주 가신 곳은 ${cafes.first().merchant} 였어요."
        streamWords(text, cafes.take(3).map { it.id }, collector)
    }

    private suspend fun respondAboutCategory(
        ctx: GenerationContext,
        categoryName: String,
        collector: kotlinx.coroutines.flow.FlowCollector<TokenChunk>
    ) {
        val items = ctx.recentTransactions.filter { it.category == categoryName }
        if (items.isEmpty()) {
            streamWords("이번 달 ${categoryName} 거래는 아직 보이지 않아요.", emptyList(), collector)
            return
        }
        val total = items.sumOf { -it.amount }
        val text = "${categoryName} 카테고리에서 ${items.size}건, 합계 ${formatWon(total)} 쓰셨어요. " +
            "가장 큰 건은 ${items.minByOrNull { it.amount }?.merchant ?: "기타"} 였어요."
        streamWords(text, items.take(3).map { it.id }, collector)
    }

    private suspend fun respondAboutSubscriptions(
        ctx: GenerationContext,
        collector: kotlinx.coroutines.flow.FlowCollector<TokenChunk>
    ) {
        val subs = ctx.recentTransactions.filter { it.category == "구독" }
        if (subs.isEmpty()) {
            streamWords("등록된 구독 결제가 아직 없어요.", emptyList(), collector)
            return
        }
        val total = subs.sumOf { -it.amount }
        val text = "활성 구독 ${subs.size}건, 월 ${formatWon(total)} 빠지고 있어요. " +
            "Netflix 가족·개인이 동시 결제되는 패턴은 한 쪽 정리하시면 절약돼요."
        streamWords(text, subs.take(3).map { it.id }, collector)
    }

    private suspend fun respondAboutDuplicates(
        ctx: GenerationContext,
        collector: kotlinx.coroutines.flow.FlowCollector<TokenChunk>
    ) {
        val text = "이번 데이터에서 의심되는 중복 결제를 찾고 있어요. " +
            "Netflix 가족·개인 동시 결제가 있어서 한 쪽 정리하면 월 17,000원 절약 가능해요."
        streamWords(text, emptyList(), collector)
    }

    private suspend fun respondGeneric(
        ctx: GenerationContext,
        collector: kotlinx.coroutines.flow.FlowCollector<TokenChunk>
    ) {
        val total = ctx.monthSummary?.totalSpend ?: 0L
        val text = "좋아요, 함께 살펴볼게요. 현재 이번 달 총 지출 ${formatWon(-total)} 기준이에요. " +
            "구체적으로 어느 카테고리·기간을 보고 싶으세요?"
        streamWords(text, emptyList(), collector)
    }

    private fun refusalResponse() =
        "AssetCoach 는 종목·상품 추천이나 수익률 예측을 하지 않아요. " +
        "거래 데이터 기반의 패턴 분석은 도와드릴 수 있어요. " +
        "예를 들어 '이번 달 외식이 얼마예요?' 같은 질문을 해보세요."

    private fun containsBlockedQuery(msg: String): Boolean {
        val keywords = listOf(
            "주식 추천", "종목 추천", "사면 좋", "투자 종목", "오를", "수익률",
            "어떤 펀드", "보험 추천", "암보험"
        )
        return keywords.any { msg.contains(it, ignoreCase = true) }
    }

    private suspend fun streamWords(
        text: String,
        citationIds: List<Long>,
        collector: kotlinx.coroutines.flow.FlowCollector<TokenChunk>
    ) {
        // 30ms 간격으로 한 글자씩 — 실 Gemma TPS ≈ 30 모방
        var idx = 0
        while (idx < text.length) {
            val chunkSize = if (text[idx].isWhitespace()) 1 else 2
            val end = (idx + chunkSize).coerceAtMost(text.length)
            collector.emit(TokenChunk(text = text.substring(idx, end)))
            delay(30)
            idx = end
        }
        collector.emit(TokenChunk(text = "", isFinal = true, citationTxIds = citationIds))
    }

    private fun formatWon(amount: Long): String {
        val abs = kotlin.math.abs(amount)
        return when {
            abs >= 10_000 -> "%,d만원".format(abs / 10_000)
            else -> "%,d원".format(abs)
        }
    }
}
