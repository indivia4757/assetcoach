package com.assetcoach.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.assetcoach.ai.GemmaClient
import com.assetcoach.ai.GenerationContext
import com.assetcoach.ai.HistoryTurn
import com.assetcoach.ai.MonthSummary
import com.assetcoach.ai.SafetyGuard
import com.assetcoach.ai.SystemPrompt
import com.assetcoach.ai.TxBrief
import com.assetcoach.data.repo.TransactionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * 채팅 ViewModel — 사용자 메시지 → Gemma 추론 → 토큰 스트리밍 → UI 갱신.
 *
 * 1) 사용자 입력을 SafetyGuard 검사 (Phase 3 에서는 input 은 거의 통과)
 * 2) 거래 DB 에서 최근 컨텍스트 추출
 * 3) 시스템 프롬프트 + 컨텍스트 + 히스토리 → GemmaClient.generate
 * 4) 토큰 스트리밍 받아 마지막 메시지에 누적
 * 5) 최종 출력에 SafetyGuard 검열 적용
 */
class ChatViewModel(
    private val gemma: GemmaClient,
    private val txRepo: TransactionRepository,
    private val segmentId: String,
    private val nameLabel: String
) : ViewModel() {

    private val _state = MutableStateFlow(ChatUiState())
    val state: StateFlow<ChatUiState> = _state

    private var generationJob: Job? = null

    init {
        val greeting = ChatMessage(
            id = -1L,
            role = ChatRole.Coach,
            text = "안녕하세요, ${nameLabel}님.\n오늘은 무엇을 함께 볼까요?",
            isStreaming = false
        )
        _state.update { it.copy(messages = listOf(greeting)) }
    }

    fun send(input: String) {
        val trimmed = input.trim()
        if (trimmed.isBlank() || _state.value.isStreaming) return

        val userMsg = ChatMessage(
            id = System.currentTimeMillis(),
            role = ChatRole.User,
            text = trimmed,
            isStreaming = false
        )
        _state.update { it.copy(messages = it.messages + userMsg) }

        // 빈 코치 메시지 (스트리밍 자리표시자)
        val coachMsgId = System.currentTimeMillis() + 1
        val coachStub = ChatMessage(
            id = coachMsgId,
            role = ChatRole.Coach,
            text = "",
            isStreaming = true
        )
        _state.update { it.copy(messages = it.messages + coachStub, isStreaming = true) }

        generationJob = viewModelScope.launch {
            try {
                val context = buildContext()
                val systemPrompt = SystemPrompt.forSegment(segmentId, nameLabel)
                val history = _state.value.messages.dropLast(1).takeLast(12).map {
                    HistoryTurn(role = if (it.role == ChatRole.User) "user" else "coach", content = it.text)
                }

                val accumulator = StringBuilder()
                var citations: List<Long> = emptyList()

                gemma.generate(trimmed, systemPrompt, history, context).collect { chunk ->
                    if (chunk.text.isNotEmpty()) {
                        accumulator.append(chunk.text)
                        _state.update { st ->
                            st.copy(messages = st.messages.map { m ->
                                if (m.id == coachMsgId) m.copy(text = accumulator.toString())
                                else m
                            })
                        }
                    }
                    if (chunk.isFinal) {
                        citations = chunk.citationTxIds
                    }
                }

                // SafetyGuard 후처리
                val finalText = when (val outcome = SafetyGuard.checkAiResponse(accumulator.toString())) {
                    is SafetyGuard.Outcome -> when (outcome.verdict) {
                        SafetyGuard.Verdict.Block -> "이 질문에는 직접 답변드리기 어려워요. " +
                            "거래 데이터 기반의 패턴 분석은 도와드릴 수 있어요."
                        SafetyGuard.Verdict.Sanitize -> outcome.sanitized ?: accumulator.toString()
                        SafetyGuard.Verdict.Pass -> accumulator.toString()
                    }
                }

                // citation 첨부
                val citationTxs = if (citations.isNotEmpty()) {
                    val all = txRepo.observeTransactions().first()
                    all.filter { it.id in citations }.map {
                        CitationItem(
                            txId = it.id,
                            line = "${formatDate(it.ts)} · ${it.merchantNorm} · ${formatWon(it.amount)}"
                        )
                    }
                } else emptyList()

                _state.update { st ->
                    st.copy(
                        messages = st.messages.map { m ->
                            if (m.id == coachMsgId) m.copy(
                                text = finalText,
                                isStreaming = false,
                                citations = citationTxs
                            ) else m
                        },
                        isStreaming = false
                    )
                }
            } catch (e: Exception) {
                _state.update { st ->
                    st.copy(
                        messages = st.messages.map { m ->
                            if (m.id == coachMsgId) m.copy(
                                text = "응답 생성 중 오류가 났어요. 다시 시도해 주세요.",
                                isStreaming = false
                            ) else m
                        },
                        isStreaming = false
                    )
                }
            }
        }
    }

    fun cancel() {
        generationJob?.cancel()
        _state.update { it.copy(isStreaming = false) }
    }

    private suspend fun buildContext(): GenerationContext {
        val txs = txRepo.observeTransactions().first()
        val cats = txRepo.observeCategories().first().associateBy { it.id }
        val brief = txs.map {
            TxBrief(
                id = it.id,
                date = formatDate(it.ts),
                merchant = it.merchantNorm.ifBlank { it.rawText },
                amount = it.amount,
                category = cats[it.categoryId]?.name ?: "기타"
            )
        }
        val totalSpend = txs.filter { it.amount < 0 }.sumOf { it.amount }
        val byCategory = txs.filter { it.amount < 0 }
            .groupBy { it.categoryId }
            .map { (catId, list) -> (cats[catId]?.name ?: "기타") to (-list.sumOf { it.amount }) }
            .sortedByDescending { it.second }

        return GenerationContext(
            recentTransactions = brief,
            monthSummary = MonthSummary(
                totalSpend = totalSpend,
                previousMonthSpend = null,
                topCategories = byCategory.take(5)
            ),
            segmentBenchmark = null
        )
    }

    private val dateFmt = SimpleDateFormat("M월 d일", Locale.KOREA).apply {
        timeZone = TimeZone.getTimeZone("Asia/Seoul")
    }

    private fun formatDate(epoch: Long): String = dateFmt.format(Date(epoch))

    private fun formatWon(amount: Long): String {
        val abs = kotlin.math.abs(amount)
        val sign = if (amount < 0) "−" else ""
        return when {
            abs >= 10_000 -> "$sign%,d만원".format(abs / 10_000)
            else -> "$sign%,d원".format(abs)
        }
    }

    class Factory(
        private val gemma: GemmaClient,
        private val repo: TransactionRepository,
        private val segmentId: String,
        private val nameLabel: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(ChatViewModel::class.java))
            return ChatViewModel(gemma, repo, segmentId, nameLabel) as T
        }
    }
}

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isStreaming: Boolean = false,
    val suggestedPrompts: List<String> = listOf(
        "이번 달 왜 많이 썼어?",
        "외식이 얼마나 됐어?",
        "구독 정리할 게 있어?",
        "스타벅스 얼마 썼어?",
        "중복 결제 보여줘"
    )
)

enum class ChatRole { User, Coach }

data class ChatMessage(
    val id: Long,
    val role: ChatRole,
    val text: String,
    val isStreaming: Boolean = false,
    val citations: List<CitationItem> = emptyList()
)

data class CitationItem(
    val txId: Long,
    val line: String
)
