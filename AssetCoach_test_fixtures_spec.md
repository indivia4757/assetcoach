# AssetCoach 합성 테스트 데이터셋 사양

> **단계**: Pre-M1 운영 산출물 (M2 까지 PM 핵심 큐레이션 의존성)
> **작성일**: 2026-05-06
> **목적**: 18 세그먼트 회귀 테스트, CSV·SMS 파서 골든 데이터셋, AI 호출 회귀(A1~A5) 정답지 큐레이션
> **양**: 18 세그먼트 × 3개월 × 100~150건 ≈ **약 7,000건의 합성 거래** + SMS 알림 100+ 케이스 + 골든 출력 약 18종

---

## 🎯 목적

기술 아키텍처 §14.2에서 요구한 회귀 테스트 인프라 구축:
- 매 빌드마다 18 세그먼트 자동 회귀 (홈 위젯 변주·인사이트 카드·상담 톤)
- CSV 파서 7종 골든 비교
- SMS 파서 정규식 + LLM 폴백 검증
- 카테고리 분류기 정확도 측정
- 중복 거래 감지 정확도 측정
- AI 호출 (A1~A5) 출력 회귀

**합성 데이터의 이유**: 실 사용자 데이터는 프라이버시·법적 이유로 사용 불가. 합성은 통제 가능·재현 가능·공유 가능.

---

## 📁 폴더 구조

```
test-fixtures/
│
├─ csv/                              # 카드사·은행 7종 CSV 골든
│   ├─ shinhan-card/
│   │   ├─ S06-2026-Q1.csv          # 민호 1분기 (90 건)
│   │   ├─ S06-2026-Q1-expected.json
│   │   └─ ...
│   ├─ samsung-card/...
│   ├─ hyundai-card/...
│   ├─ kb-card/...
│   ├─ shinhan-bank/...
│   ├─ kb-bank/...
│   └─ woori-bank/...
│
├─ sms/                              # Android Notification 샘플
│   ├─ shinhan-card-001.txt          # 원문 발신 텍스트 + expected.json
│   ├─ shinhan-card-002.txt
│   ├─ ...                            # 100+ 케이스
│   └─ edge-cases/                   # 파싱 어려운 케이스
│       ├─ multipart-001.txt         # 분할 발신
│       ├─ refund-001.txt            # 환불·취소
│       ├─ foreign-currency-001.txt  # 해외 결제
│       └─ ...
│
├─ segments/                         # 18 세그먼트별 시나리오
│   ├─ S01-juneun-26.json            # 사회초년생 지훈
│   ├─ S04-soobin-29.json            # 프리랜서 수빈
│   ├─ S06-minho-34.json             # 영유아 민호 (기존 페르소나)
│   ├─ S09-kyungsook-49.json         # 교육·노후 병행
│   ├─ S17-youngho-67.json           # 은퇴 영호
│   └─ ...                            # 18종
│
├─ ai-golden/                        # AI 호출 골든 출력
│   ├─ A1-insight/                   # 홈 인사이트 카드
│   │   ├─ S06-2026-04-output.json
│   │   └─ ...
│   ├─ A2-chat/                       # 상담 응답 (50 prompts × 3 segments)
│   ├─ A3-report/                     # 주간·월간 리포트
│   ├─ A4-classify/                   # 카테고리 분류
│   └─ A5-suggest/                    # 추천 질문
│
├─ benchmarks/                       # 세그먼트 평균 통계
│   └─ segment-benchmarks-2026.json  # 18 세그먼트 카테고리별 평균
│
└─ generators/                       # 데이터 생성 스크립트
    ├─ generate_segment.py           # 세그먼트 → 거래 시퀀스
    ├─ generate_csv.py               # 거래 → 7종 CSV 포맷
    └─ generate_sms.py               # 거래 → SMS 발신 텍스트
```

---

## 📋 18 세그먼트별 시나리오 사양

각 시나리오 파일(`segments/SXX-name-age.json`):

```json
{
  "segment_id": "S06",
  "name_label": "민호",
  "age": 34,
  "L": "L3",
  "I": "I1",
  "H": "H3a",
  "household_size": 4,
  "child_ages": [3, 6],
  "monthly_income": 8200000,
  "spouse_income": 4800000,
  "household_total_income": 13000000,
  "fixed_costs": {
    "주거": 1800000,
    "통신": 180000,
    "보험": 420000,
    "어린이집": 800000,
    "구독": 84500
  },
  "variable_costs_avg": {
    "식비": 820000,
    "외식": 280000,
    "교통": 240000,
    "의료": 120000,
    "쇼핑": 350000,
    "여가": 220000,
    "기타": 180000
  },
  "monthly_savings": 2100000,
  "assets": {
    "현금예금": 32000000,
    "전세보증금": 350000000,
    "투자": 28000000,
    "퇴직연금": 12000000
  },
  "tendencies": {
    "consumption": 3,
    "engagement": 4,
    "risk": 3
  },
  "scenario_notes": [
    "4월에 어린이날 지출 +12만 (선물·외식)",
    "Netflix 가족·개인 중복 결제 (의도된 결함)",
    "건조기 1회성 구매 1,290,000원 (4/8)",
    "사교육비 학원 한 곳 휴원으로 -5%"
  ],
  "transaction_count_3m": 312,
  "expected_segment_classification": "S06",
  "expected_top_widgets": ["family_health_score", "duplicate_subscription"],
  "expected_first_insight": "중복 결제 2건 발견"
}
```

### 18 세그먼트 시나리오 큐레이션 우선순위

PM 큐레이션 작업 순서 (M2 까지):

| 우선 | 세그먼트 | 큐레이션 사유 |
|---|---|---|
| **P0** | S01 지훈 / S04 수빈 / S06 민호 / S09 경숙 / S17 영호 | 와이어프레임에 5종 변주 명시 — 가장 먼저 구현 |
| **P1** | S02 / S05 / S08 / S12 / S14 | 청년·DINK·전문직·은퇴 전환 핵심 |
| **P2** | 나머지 8종 | M5~M7 사이에 보강 |

각 시나리오는 다음을 반드시 포함:
- 일반 흐름 거래 (예측 가능)
- 의도된 이상 (중복·미사용 구독·평소보다 큰 거래)
- 계절성 이벤트 1회 (어린이날·연말정산·휴가·명절)

---

## 💳 CSV 포맷 7종 사양

상세는 `AssetCoach_csv_format_specs.md` (별도 산출물). 여기는 테스트 골든 명세만.

### 골든 입력·출력 페어

각 카드사·은행 폴더에 다음 페어:

```
shinhan-card/
├─ S06-2026-Q1.csv               # 입력 CSV (90 건)
└─ S06-2026-Q1-expected.json     # 기대 파싱 결과
```

`expected.json` 구조:

```json
{
  "format": "shinhan-card-v1",
  "transaction_count": 90,
  "transactions": [
    {
      "ts": "2026-04-12T14:23:00+09:00",
      "amount": -68000,
      "raw_text": "패밀리 레스토랑 강남",
      "merchant_norm": "패밀리 레스토랑",
      "category_id": 1,
      "category_name": "외식",
      "category_confidence": 0.95,
      "category_source": "rule"
    }
    ...
  ],
  "duplicate_groups": [
    {
      "ids": ["t-2026-04-12-001", "t-2026-04-15-014"],
      "reason": "Netflix 가족·개인 동시 결제"
    }
  ],
  "category_distribution": {
    "주거": 1800000,
    "식비": 820000,
    ...
  }
}
```

### 회귀 검증 항목

| 검증 | 기준 |
|---|---|
| 거래 건수 | 정확히 일치 |
| 거래 timestamp / 금액 | 정확히 일치 |
| 카테고리 분류 정확도 | ≥ 90% (각 거래) |
| 중복 그룹 식별 | 100% (의도된 중복은 모두 감지) |
| 인코딩 (CP949 / UTF-8) | 둘 다 동일 결과 |

---

## 📱 SMS 알림 골든 (Android)

### 정규식 패턴 + 폴백 LLM 검증

각 SMS 케이스 페어:

```
sms/shinhan-card-001/
├─ raw.txt           # 원문 알림 (UTF-8)
└─ expected.json     # 파싱 결과
```

`raw.txt` 예시:
```
[Web발신]
신한카드 승인 김XX님
5,800원 04/22 14:23
스타벅스 종로점
```

`expected.json`:
```json
{
  "card_issuer": "신한카드",
  "amount": -5800,
  "timestamp": "2026-04-22T14:23:00+09:00",
  "merchant_raw": "스타벅스 종로점",
  "merchant_norm": "스타벅스",
  "category_id": 2,
  "category_name": "카페",
  "parser": "regex",
  "confidence": 0.99
}
```

### 케이스 매트릭스 (100+ 종)

| 카드/은행 | 일반 결제 | 환불 | 분할 발신 | 외화 | 해외 가맹점 | 정기결제 | 거절 |
|---|---|---|---|---|---|---|---|
| 신한카드 | 10 | 3 | 2 | 2 | 2 | 2 | 1 |
| 삼성카드 | 10 | 3 | 2 | 2 | 2 | 2 | 1 |
| 현대카드 | 10 | 3 | 2 | 2 | 2 | 2 | 1 |
| KB카드 | 10 | 3 | 2 | 2 | 2 | 2 | 1 |
| 신한은행 | 5 | 2 | 0 | 0 | 0 | 0 | 1 |
| KB은행 | 5 | 2 | 0 | 0 | 0 | 0 | 1 |
| 우리은행 | 5 | 2 | 0 | 0 | 0 | 0 | 1 |
| **합계** | **55** | **18** | **8** | **8** | **8** | **8** | **6** | = **111건** |

### 엣지 케이스 (`edge-cases/` 폴더)
- 한 거래가 2~3개 SMS로 분할 (정책 변경 카드사)
- 환불·취소 (음수 금액 또는 별도 처리)
- 외화 결제 (USD·JPY 표시)
- 해외 가맹점명 (영문)
- 가맹점명에 특수문자·공백 다수
- 한도 초과·거절
- 카드 도용 의심 알림 (거래 X)

각 엣지 케이스는 `expected.json`에 `parser: "llm"` 로 표기, A4 호출이 폴백되었음을 명시.

---

## 🤖 AI 호출 골든 (A1~A5)

### A1 인사이트 카드 골든

```
ai-golden/A1-insight/
├─ S06-2026-04.input.json     # 컨텍스트 (월 통계 요약)
└─ S06-2026-04.golden.json    # 기대 출력 (3장 카드)
```

`golden.json`:
```json
{
  "cards": [
    {
      "type": "discovery",
      "eyebrow": "발견",
      "head": "중복 결제 2건 찾았어요",
      "body": "Netflix 가족·개인이 동시에 빠지고 있어요. 한 쪽 정리하면 월 17,000원 절약.",
      "must_include_phrases": ["중복", "Netflix"],
      "must_avoid_phrases": ["반드시 해지하세요", "투자"]
    },
    ...
  ]
}
```

회귀 비교: must_include 모두 포함 + must_avoid 모두 회피 → Pass.

### A2 상담 골든
50개 프롬프트 (Gemma 4 PoC와 공유) × 3 세그먼트 = 150건 정답 응답 패턴.
자유 응답이라 정확 매치는 어려우니 **루브릭 점수 기준**으로 회귀 (PoC 평가 §평가 루브릭).

### A3 리포트 골든
세그먼트 × 주차 조합 약 18종. 매거진 톤·길이·섹션 구성 검증.

### A4 카테고리 분류 골든
500건 가맹점 → 카테고리 매핑 테이블. 회귀 정확도 ≥ 95% 요구.

### A5 추천 질문 골든
세그먼트 × 시즌 약 72종 (18 × 4). 출력 5개 질문이 (a) 세그먼트 톤 (b) 시즌 적합 (c) 길이 적정 모두 만족.

---

## 📊 세그먼트 벤치마크 (`segment-benchmarks-2026.json`)

홈 화면 "S06 평균 대비" 비교에 사용. 통계청·금감원·카드사 공개 자료 + PM 추정 기반.

```json
{
  "version": "2026.1",
  "source_notes": "통계청 가계동향조사 2025 + 금감원 가계지출 통계 + 카드사 공개 데이터 기반 추정",
  "segments": {
    "S06": {
      "monthly_household_avg": {
        "주거": 1750000,
        "식비": 850000,
        "외식": 280000,
        "사교육": 950000,
        "교통": 220000,
        ...
      },
      "savings_rate_avg": 0.16,
      "fixed_cost_ratio_avg": 0.42,
      "emergency_months_median": 4.2,
      "financial_health_score_avg": 72
    },
    ...
  }
}
```

**갱신 주기**: 분기. PM이 통계청·금감원 신규 자료 발표 시 갱신.

**중요**: 벤치마크가 부정확하면 모든 인사이트가 부정확 → AI 응답 품질에 직접 영향. **법무 검수 필수** (특정 업체 데이터 인용 시 출처 명시).

---

## 🔧 데이터 생성 스크립트

### `generate_segment.py`

```python
def generate_segment_transactions(scenario_path, months=3, seed=42):
    """
    세그먼트 시나리오 JSON → 합성 거래 시퀀스 생성
    
    - 고정비는 매월 동일 일자
    - 변동비는 주중·주말·계절성 분포 따라 무작위
    - 시나리오의 '의도된 이상' (중복·일회성 큰 거래) 삽입
    - seed 고정으로 재현성 보장
    """
    ...
```

### `generate_csv.py`

```python
def to_shinhan_card_csv(transactions, output_path, encoding='cp949'):
    """거래 시퀀스 → 신한카드 CSV 포맷 (CP949 인코딩)"""
    ...

def to_kb_bank_excel(transactions, output_path):
    """거래 시퀀스 → KB은행 거래내역 Excel"""
    ...
```

### `generate_sms.py`

```python
def to_sms_notification(tx, card_issuer):
    """단건 거래 → 카드사 SMS 알림 텍스트"""
    return f"""[Web발신]
{card_issuer} 승인 김XX님
{tx.amount:,}원 {tx.ts.strftime('%m/%d %H:%M')}
{tx.merchant_raw}"""
```

---

## ✅ 큐레이션 완료 기준 (M2 게이트)

| 항목 | 완료 기준 |
|---|---|
| **18 세그먼트 시나리오** | 모두 `.json` 작성, 가구 소득·고정비·변동비·자산 합리적 |
| **P0 5개 세그먼트** | 3개월 거래 데이터셋 생성 완료 (총 약 1,500건) |
| **CSV 7종 골든** | 각 카드/은행 1세트씩 (P0 세그먼트 기준) |
| **SMS 골든** | 100건+ 정규식 매칭 + 11건 엣지 케이스 |
| **벤치마크 v1** | 18 세그먼트 카테고리별 평균값 + 출처 메모 |
| **A1·A4·A5 골든** | 각 호출별 P0 세그먼트 분량 |
| **생성 스크립트** | seed 고정 재현성 검증 |

---

## 🚧 리스크

| 리스크 | 영향 | 완화 |
|---|---|---|
| 세그먼트 시나리오의 비현실성 (PM 추정 의존) | AI 응답 품질이 합성 데이터에 과적합 | 알파(M9)에서 실 사용자 데이터로 정합성 검증 |
| 벤치마크 출처 부정확 | 인사이트 신뢰성 손상 | 통계청·금감원 1차 자료만 사용, 가공 데이터 회피 |
| 합성 SMS가 실 카드사 발신 형식과 미세 차이 | 정규식 누수 | 알파에서 실 사용자 발신 텍스트 익명 수집 (옵트인) |

---

## 🔁 갱신 정책

- **시나리오 JSON**: 변경 시 git diff로 추적
- **벤치마크**: 분기 1회 갱신, 변경 이력은 별도 changelog
- **AI 골든**: 모델 메이저 버전 변경 시 전면 재생성

---

*이 데이터셋이 없으면 18 세그먼트 회귀 자체가 불가능 — M2까지 PM 핵심 작업.*
