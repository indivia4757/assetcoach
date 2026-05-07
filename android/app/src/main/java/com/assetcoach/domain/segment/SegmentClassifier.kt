package com.assetcoach.domain.segment

/**
 * 18 세그먼트 매트릭스 — Q1-Q4 답변 → S01~S18 매핑.
 * 출처: AssetCoach_segment_matrix.md §1-B 실질 세그먼트 카탈로그.
 */
object SegmentClassifier {

    enum class LifeStage(val code: String, val koreanLabel: String, val ageRange: IntRange) {
        L1("L1", "형성기", 19..25),
        L2("L2", "축적 초기", 26..33),
        L3("L3", "축적 본격기", 34..43),
        L4("L4", "교육·노후 병행기", 44..53),
        L5("L5", "은퇴 전환기", 54..64),
        L6("L6", "은퇴 생활기", 65..120);

        companion object {
            fun fromAge(age: Int): LifeStage = entries.first { age in it.ageRange }
        }
    }

    enum class IncomePattern(val code: String, val koreanLabel: String) {
        I1("I1", "월급 안정형"),
        I2("I2", "월급 특수형"),
        I3("I3", "변동 수입형"),
        I4("I4", "고소득 전문직"),
        I5("I5", "비정기 수입"),
        I6("I6", "연금·자산 수입")
    }

    enum class Household(val code: String, val koreanLabel: String) {
        H1("H1", "1인 가구"),
        H2("H2", "커플 (DINK)"),
        H3a("H3a", "자녀 영유아 (0-7세)"),
        H3b("H3b", "자녀 초중 (8-15세)"),
        H3c("H3c", "자녀 고등·대학 (16-22세)"),
        H3d("H3d", "자녀 성인 독립"),
        H4("H4", "3세대 동거")
    }

    data class Result(
        val segmentId: String,
        val displayMode: String,    // normal / retirement / freelance
        val lifeStage: LifeStage,
        val incomePattern: IncomePattern,
        val household: Household,
        val confidence: Float = 1.0f
    )

    /**
     * Q1-Q4 답변 → 18 세그먼트 중 가장 가까운 것 매핑.
     * 표에 없는 조합은 가장 가까운 세그먼트로 흡수 (fallback).
     */
    fun classify(
        birthYear: Int,
        currentYear: Int = 2026,
        income: IncomePattern,
        household: Household,
        childAges: List<Int> = emptyList()
    ): Result {
        val age = currentYear - birthYear
        val L = LifeStage.fromAge(age)

        // 자녀 연령으로 H3 세부 분류 보정
        val H = if (childAges.isNotEmpty() && household.code.startsWith("H3")) {
            when (childAges.maxOrNull() ?: 0) {
                in 0..7 -> Household.H3a
                in 8..15 -> Household.H3b
                in 16..22 -> Household.H3c
                else -> Household.H3d
            }
        } else household

        val segmentId = lookupTable(L, income, H) ?: nearestFallback(L, income, H)

        val mode = when {
            L == LifeStage.L5 || L == LifeStage.L6 || income == IncomePattern.I6 -> "retirement"
            income == IncomePattern.I3 || income == IncomePattern.I5 -> "freelance"
            else -> "normal"
        }

        return Result(
            segmentId = segmentId,
            displayMode = mode,
            lifeStage = L,
            incomePattern = income,
            household = H
        )
    }

    /**
     * 18 세그먼트 정확 매칭. 출처: segment_matrix.md §1-B.
     * 없으면 null → fallback 호출.
     */
    private fun lookupTable(L: LifeStage, I: IncomePattern, H: Household): String? = when {
        L == LifeStage.L1 && I == IncomePattern.I1 && H == Household.H1 -> "S01"
        L == LifeStage.L1 && I == IncomePattern.I5 && H == Household.H1 -> "S02"
        L == LifeStage.L2 && I == IncomePattern.I1 && H == Household.H1 -> "S03"
        L == LifeStage.L2 && I == IncomePattern.I3 && H == Household.H1 -> "S04"
        (L == LifeStage.L2 || L == LifeStage.L3) && I == IncomePattern.I1 && H == Household.H2 -> "S05"
        L == LifeStage.L3 && I == IncomePattern.I1 && H == Household.H3a -> "S06"
        L == LifeStage.L3 && I == IncomePattern.I1 && H == Household.H3b -> "S07"
        L == LifeStage.L3 && I == IncomePattern.I3 && H.code.startsWith("H3") -> "S08"
        L == LifeStage.L4 && I == IncomePattern.I1 && (H == Household.H3b || H == Household.H3c) -> "S09"
        L == LifeStage.L4 && I == IncomePattern.I2 -> "S10"
        L == LifeStage.L4 && I == IncomePattern.I4 -> "S11"
        I == IncomePattern.I4 && (L == LifeStage.L3 || L == LifeStage.L4) -> "S12"
        L == LifeStage.L4 && H == Household.H4 -> "S13"
        L == LifeStage.L5 && I != IncomePattern.I6 -> "S14"
        L == LifeStage.L5 && I == IncomePattern.I6 -> "S15"
        L == LifeStage.L6 && H == Household.H2 -> "S16"
        L == LifeStage.L6 && H == Household.H1 -> "S17"
        L == LifeStage.L6 && I == IncomePattern.I6 -> "S18"
        else -> null
    }

    /**
     * 표에 없는 조합 — 가장 가까운 세그먼트로 흡수.
     */
    private fun nearestFallback(L: LifeStage, I: IncomePattern, H: Household): String =
        when (L) {
            LifeStage.L1 -> if (H == Household.H1) "S01" else "S02"
            LifeStage.L2 -> if (I == IncomePattern.I3) "S04" else "S03"
            LifeStage.L3 -> if (H.code.startsWith("H3")) "S06" else "S05"
            LifeStage.L4 -> "S09"
            LifeStage.L5 -> "S14"
            LifeStage.L6 -> "S17"
        }
}
