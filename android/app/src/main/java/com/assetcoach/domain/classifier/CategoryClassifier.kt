package com.assetcoach.domain.classifier

/**
 * 가맹점·메모 → 카테고리 ID + 신뢰도.
 *
 * 정책:
 *  1. 정규화된 가맹점명 → 시드 매핑 검색 (높은 신뢰도)
 *  2. 키워드 룰 (중간 신뢰도)
 *  3. 기타 (id=99, 신뢰도 0.3)
 *
 * Phase 3: 신뢰도 < 0.7 면 Gemma 4 LLM 폴백 (A4 호출).
 */
object CategoryClassifier {

    data class Result(val categoryId: Int, val confidence: Float, val source: String)

    // 정규화된 가맹점명 → 카테고리 ID
    private val merchantMap: Map<String, Int> = mapOf(
        // 카페 (2)
        "스타벅스" to 2, "starbucks" to 2,
        "투썸플레이스" to 2, "투썸" to 2,
        "이디야" to 2, "이디야커피" to 2,
        "메가커피" to 2, "메가엠지씨커피" to 2,
        "할리스" to 2, "할리스커피" to 2,
        "파스쿠찌" to 2,
        "공차" to 2,
        "엔젤리너스" to 2,
        "폴바셋" to 2,
        "블루보틀" to 2,
        "커피빈" to 2,

        // 식비 (1) — 편의점·마트 등
        "GS25" to 1, "gs25" to 1,
        "CU" to 1, "cu" to 1, "씨유" to 1,
        "세븐일레븐" to 1, "7-eleven" to 1,
        "이마트24" to 1,
        "미니스톱" to 1,
        "이마트" to 1,
        "홈플러스" to 1,
        "롯데마트" to 1,
        "농협하나로마트" to 1, "하나로마트" to 1,
        "코스트코" to 1,
        "SSG닷컴" to 1, "쓱닷컴" to 1,
        "마켓컬리" to 1, "컬리" to 1,
        "쿠팡이츠" to 1,

        // 외식 (3)
        "패밀리 레스토랑" to 3,
        "더킹스 도토리" to 3,
        "맥도날드" to 3, "맥날" to 3,
        "버거킹" to 3,
        "롯데리아" to 3,
        "KFC" to 3, "kfc" to 3,
        "서브웨이" to 3,
        "도미노피자" to 3,
        "피자헛" to 3,
        "본죽" to 3,
        "김밥천국" to 3,

        // 교통 (4)
        "쏘카" to 4, "socar" to 4,
        "그린카" to 4,
        "카카오T" to 4, "카카오 택시" to 4,
        "우티" to 4,

        // 쇼핑 (5)
        "쿠팡" to 5, "쿠팡로켓" to 5, "쿠팡결제" to 5,
        "11번가" to 5,
        "지마켓" to 5, "G마켓" to 5,
        "옥션" to 5,
        "현대백화점" to 5,
        "신세계백화점" to 5,
        "롯데백화점" to 5,
        "갤러리아" to 5,

        // 통신 (8)
        "SKT" to 8, "SK텔레콤" to 8,
        "KT" to 8,
        "LG U+" to 8, "LGU+" to 8, "LG유플러스" to 8,
        "알뜰모바일" to 8,

        // 구독 (11)
        "Netflix" to 11, "넷플릭스" to 11, "netflix" to 11,
        "Spotify" to 11, "스포티파이" to 11,
        "Apple Music" to 11, "애플뮤직" to 11,
        "멜론" to 11,
        "지니뮤직" to 11,
        "ChatGPT" to 11, "chatgpt" to 11, "OpenAI" to 11,
        "Disney+" to 11, "디즈니플러스" to 11,
        "Tving" to 11, "티빙" to 11,
        "Wavve" to 11, "웨이브" to 11,
        "쿠팡플레이" to 11,
        "왓챠" to 11,
        "유튜브 프리미엄" to 11, "Youtube Premium" to 11,
        "밀리의 서재" to 11,

        // 가전 (5 — 쇼핑으로 분류)
        "LG전자" to 5,
        "삼성전자" to 5,

        // 의료 (6)
        "약국" to 6, "온누리약국" to 6,
        "병원" to 6,

        // 교육 (7)
        "어린이집" to 14,  // 자녀양육
        "유치원" to 14,
        "학원" to 7,
        "교습소" to 7
    )

    // 키워드 룰 (가맹점명에 키워드 포함 시)
    private val keywordRules: List<Pair<Regex, Int>> = listOf(
        Regex("커피|coffee", RegexOption.IGNORE_CASE) to 2,
        Regex("카페|cafe", RegexOption.IGNORE_CASE) to 2,
        Regex("편의점") to 1,
        Regex("마트|슈퍼") to 1,
        Regex("음식|식당|레스토랑|restaurant", RegexOption.IGNORE_CASE) to 3,
        Regex("택시|taxi", RegexOption.IGNORE_CASE) to 4,
        Regex("주유소|gas station", RegexOption.IGNORE_CASE) to 4,
        Regex("백화점|department", RegexOption.IGNORE_CASE) to 5,
        Regex("의원|병원|클리닉|hospital|clinic", RegexOption.IGNORE_CASE) to 6,
        Regex("약국|pharmacy", RegexOption.IGNORE_CASE) to 6,
        Regex("학원|교습", RegexOption.IGNORE_CASE) to 7,
        Regex("통신|telecom", RegexOption.IGNORE_CASE) to 8,
        Regex("관리비|월세|전세") to 9,
        Regex("보험") to 10,
        Regex("월결제|구독|subscription", RegexOption.IGNORE_CASE) to 11
    )

    fun classify(merchantNormalized: String, memo: String? = null, amount: Long = 0): Result {
        // 1. 정확 매칭
        val exact = merchantMap[merchantNormalized]
        if (exact != null) return Result(exact, 0.95f, "rule")

        // 2. 케이스 무시 매칭
        val ci = merchantMap.entries.firstOrNull {
            it.key.equals(merchantNormalized, ignoreCase = true)
        }
        if (ci != null) return Result(ci.value, 0.92f, "rule")

        // 3. partial match
        val partial = merchantMap.entries.firstOrNull {
            merchantNormalized.contains(it.key, ignoreCase = true)
        }
        if (partial != null) return Result(partial.value, 0.85f, "rule")

        // 4. 키워드 룰
        val combined = (merchantNormalized + " " + (memo ?: "")).trim()
        for ((rx, catId) in keywordRules) {
            if (rx.containsMatchIn(combined)) {
                return Result(catId, 0.7f, "rule")
            }
        }

        // 5. 기타 fallback
        return Result(99, 0.3f, "rule")
    }
}
