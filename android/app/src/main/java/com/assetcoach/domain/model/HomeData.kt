package com.assetcoach.domain.model

/**
 * 홈 화면 1회 렌더링 분량의 정적 데이터 + 세그먼트별 변주.
 *
 * 와이어프레임 §2.4 (S06) / §2.6 (S17) / §2.3 (S04) 기반.
 * Phase 2 에서 ViewModel 이 거래 DB 로부터 동적으로 채워줄 예정.
 */
data class HomeData(
    val segmentId: String,
    val nameLabel: String,
    val displayMode: DisplayModeKind,
    val narrative: NarrativeBlock,
    val widget: WidgetBlock,
    val insights: List<InsightBlock>,
    val goals: List<GoalBlock>,
    val ctaText: String
) {
    enum class DisplayModeKind { Normal, Retirement, Freelance }
}

data class NarrativeBlock(
    val eyebrow: String,
    val firstLine: String,
    val highlight: String,
    val tail: String
)

sealed class WidgetBlock {
    abstract val eyebrow: String

    /** S06 — 게이지 + 점수 + 평균 비교 */
    data class Score(
        override val eyebrow: String,
        val score: Int,
        val maxScore: Int,
        val gaugeProgress: Float,
        val benchmarkBoldText: String,
        val benchmarkSuffixText: String
    ) : WidgetBlock()

    /** S17 — 큰 숫자 (자산 잔존 N년) + 부가 정보 */
    data class BigNumber(
        override val eyebrow: String,
        val number: String,
        val unit: String,
        val gaugeProgress: Float,
        val gaugeColorMustard: Boolean,
        val leftCaption: String,
        val rightCaption: String,
        val benchmarkText: String,
        val ctaPrimary: String?
    ) : WidgetBlock()

    /** S04 — 비상금 개월수 (현재 vs 권장 비교 바) */
    data class Months(
        override val eyebrow: String,
        val current: String,
        val unit: String,
        val currentMonths: Float,
        val recommendedMonths: Float,
        val leftCaption: String,
        val rightCaption: String,
        val benchmarkText: String
    ) : WidgetBlock()
}

data class InsightBlock(
    val eyebrow: String,
    val head: String,
    val body: String,
    val accent: AccentKind
) {
    enum class AccentKind { Terracotta, Positive, Mustard }
}

data class GoalBlock(
    val emoji: String,
    val title: String,
    val meta: String,
    val progress: Float,
    val percentLabel: String,
    val mustard: Boolean = false
)

// ─────────────────────────────────────────────────────────
// 세그먼트별 mock 데이터 (Phase 2 에서 실 거래로 대체)
// ─────────────────────────────────────────────────────────

object HomeMockData {

    // S06 영유아 가구 맞벌이 — 와이어프레임 §2.4
    val s06 = HomeData(
        segmentId = "S06",
        nameLabel = "민호",
        displayMode = HomeData.DisplayModeKind.Normal,
        narrative = NarrativeBlock(
            eyebrow = "민호님 가족",
            firstLine = "이번 달 이야기예요.\n중복 결제 두 건 빼면 지난달보다",
            highlight = " 34만원 덜 ",
            tail = "쓰셨어요."
        ),
        widget = WidgetBlock.Score(
            eyebrow = "우리 가족 재무 건강도",
            score = 78,
            maxScore = 100,
            gaugeProgress = 0.78f,
            benchmarkBoldText = "72점",
            benchmarkSuffixText = "보다 6점 높아요"
        ),
        insights = listOf(
            InsightBlock("발견", "중복 결제 2건\n찾았어요", "Netflix 가족·개인이 동시에 빠지고 있어요. 한 쪽 정리하면 월 17,000원 절약.", InsightBlock.AccentKind.Terracotta),
            InsightBlock("변화", "사교육비 −5%", "학원 한 곳 휴원으로 이번 달 사교육이 평소보다 적어요.", InsightBlock.AccentKind.Positive),
            InsightBlock("평균", "가족 외식\nS06 평균 수준", "28만원으로 같은 또래 가구와 거의 동일.", InsightBlock.AccentKind.Mustard)
        ),
        goals = listOf(
            GoalBlock("🛡", "비상금 6개월치", "720만 / 1,000만", 0.72f, "72%"),
            GoalBlock("✈", "가족 여행 자금", "45만 / 100만", 0.45f, "45%", mustard = true)
        ),
        ctaText = "AI 코치에게 물어보기"
    )

    // S17 은퇴 영호 — 와이어프레임 §2.6, 자동 은퇴 모드
    val s17 = HomeData(
        segmentId = "S17",
        nameLabel = "영호",
        displayMode = HomeData.DisplayModeKind.Retirement,
        narrative = NarrativeBlock(
            eyebrow = "영호님",
            firstLine = "오늘도 반갑습니다.\n이번 달은",
            highlight = " 평소와 비슷한 ",
            tail = "흐름이에요."
        ),
        widget = WidgetBlock.BigNumber(
            eyebrow = "현 속도로 자산 잔존",
            number = "23",
            unit = "년",
            gaugeProgress = 0.72f,
            gaugeColorMustard = true,
            leftCaption = "현재 자산 3.2억",
            rightCaption = "월 평균 인출 230만",
            benchmarkText = "S17 평균보다 4년 더 버텨요",
            ctaPrimary = "인출 계획 보기"
        ),
        insights = listOf(
            InsightBlock("평소 대비", "의료비 18만원\n+6만원", "평소 12만원 대비 늘었어요. 일회성인지 주기적인지 함께 보실래요?", InsightBlock.AccentKind.Mustard)
        ),
        goals = listOf(
            GoalBlock("💊", "의료비 비상금", "240만 / 500만", 0.48f, "48%", mustard = true)
        ),
        ctaText = "코치에게 말하기"
    )

    // S04 프리랜서 수빈 — 와이어프레임 §2.3
    val s04 = HomeData(
        segmentId = "S04",
        nameLabel = "수빈",
        displayMode = HomeData.DisplayModeKind.Freelance,
        narrative = NarrativeBlock(
            eyebrow = "수빈님",
            firstLine = "이번 달 입금이",
            highlight = " 아직 도착 ",
            tail = "하지 않았어요. 고정비 내고 나면 지난달 여유분으로 23일 버텨요."
        ),
        widget = WidgetBlock.Months(
            eyebrow = "고정비 대비 비상금 개월수",
            current = "2.3",
            unit = "개월",
            currentMonths = 2.3f,
            recommendedMonths = 6.0f,
            leftCaption = "지금 2.3",
            rightCaption = "권장 6.0",
            benchmarkText = "변동 수입은 6개월치 비상금이 안전선이에요"
        ),
        insights = listOf(
            InsightBlock("편차", "±42%", "최저 220만, 최고 540만. 분기·연간 토글로 보면 흐름이 보여요.", InsightBlock.AccentKind.Terracotta),
            InsightBlock("5월 대비", "종소세\n적립 부족 320만", "5월 종합소득세 신고까지 한 달, 별도 적립 시뮬레이션 보기.", InsightBlock.AccentKind.Mustard),
            InsightBlock("고정비 비중", "64%", "변동 수입에 비해 높아요. 줄일 만한 항목 함께 볼까요?", InsightBlock.AccentKind.Terracotta)
        ),
        goals = listOf(
            GoalBlock("🛡", "비상금 6개월치", "138만 / 360만", 0.38f, "38%"),
            GoalBlock("📋", "5월 종합소득세", "230만 / 550만", 0.42f, "42%", mustard = true)
        ),
        ctaText = "이번 달 어떻게 버틸지 보기"
    )

    val all = listOf(s06, s17, s04)
}
