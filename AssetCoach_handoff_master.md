# AssetCoach 마스터 핸드오프 (Master Handoff)

> **이 문서는 AssetCoach 프로젝트에 새로 합류하는 사람을 위한 단일 진입점입니다.**
> 처음 보는 사람은 §0 → §1 → 자기 역할의 §3 으로 이동하면 1시간 내에 풀 컨텍스트를 얻습니다.

**최신 갱신**: 2026-05-06
**프로젝트 단계**: 6단계 설계 완료 · 개발 진입 직전 (Pre-M1)

---

## 0. TL;DR — 60초 요약

AssetCoach는 **온디바이스 Gemma 4 기반 대화형 자산관리 앱**이다. CSV·SMS로 받은 거래를 18개 세그먼트(생애주기 × 수입패턴 × 가구) × 3축 성향에 맞춰 해석하고, 토스·뱅샐이 "보여주는" 데서 멈추는 지점을 "함께 생각해주는" 단계로 끌고 간다.

- **타깃**: 전 연령 × 전 직업군 (단일 페르소나 X)
- **차별점**: 프라이버시(온디바이스) · 대화형 · 세그먼트 개인화 · 벤치마크형 가이드 (법적 안전)
- **기술 핵심**: KMP 공유 코어 + iOS SwiftUI / Android Compose 네이티브 UI + Gemma 4 E4B INT4 (≈2.5GB) + SQLCipher
- **일정**: 12개월 개발 (M1~M12), 100K 사용자 목표
- **리스크 1순위**: Gemma 4 한국어 품질·성능 미검증 — Pre-M1 PoC로 1주 검증 필수

---

## 1. 6단계 설계 결과 한눈에

| 단계 | 산출물 | 파일 | 핵심 결과 |
|---|---|---|---|
| 1 | 제품 기획 | `AssetCoach_handoff_v2.md` | v2 범위 확장, 18 세그먼트, "함께 생각해주는" 포지셔닝 |
| 2 | 사용자 여정 | `AssetCoach_user_journey_v2.md` | 다중 페르소나 (지훈·수빈·민호·경숙·영호) 감정 곡선 |
| 2.5 | 세그먼트 매트릭스 | `AssetCoach_segment_matrix.md` | 18 세그먼트 + 3축 성향 + Gemma 4 시스템 프롬프트 템플릿 |
| 3 | 정보 구조 (IA) | `AssetCoach_information_architecture.md` | 4탭 (홈·분석·상담·목표) · 7단계 온보딩 · 세그먼트별 동적 구성 |
| 3.5 | 쇼케이스 매거진 | `AssetCoach_showcase.html` | 8 챕터 에디토리얼 — 이해관계자 시연용 |
| 4 | 디자인 시스템 | `AssetCoach_design_system.md` | 컬러 11종 · 타입 11스케일 · 컴포넌트 카탈로그 · 세그먼트 변주 + CSS 토큰 |
| 5 | 와이어프레임 + 프로토타입 | `AssetCoach_wireframes.md` + `AssetCoach_prototype.html` | 31 화면 · F1~F4 Critical Path · 클릭 가능한 모바일 mock |
| 6 | 기술 아키텍처 | `AssetCoach_technical_architecture.md` | KMP · MediaPipe LLM · 5개 AI 호출 매트릭스 · 12개월 마일스톤 |

부속 자료: `AssetCoach_app_icon.svg` · `AssetCoach_gemma4_poc.md`

---

## 2. 핵심 설계 결정 치트시트

| 영역 | 결정 |
|---|---|
| **타깃** | 전 연령 × 전 직업군 (성별 기반 조언 명시 X) |
| **세그먼트** | 18개 (생애주기 6 × 수입 6 × 가구 7 → 압축) + 성향 3축 |
| **레퍼런스 페르소나** | S06 민호 (영유아 가구 맞벌이) |
| **포지셔닝** | "함께 생각해주는" — 토스/뱅샐의 "보여주는" 위에 한 층 더 |
| **AI 모델** | Gemma 4 E4B (Apache 2.0) INT4 양자화 (Q4_K_M, ≈2.5GB) |
| **추론 엔진** | MediaPipe LLM Inference (iOS·Android 공통) |
| **앱 스택** | iOS SwiftUI 5 + Android Compose + KMP 공유 도메인·데이터 코어 |
| **로컬 DB** | SQLite + SQLCipher (디바이스 keystore 키) |
| **백업** | iCloud Private DB / Google Drive 앱 폴더 (사전 암호화) |
| **데이터 수집** | CSV 7종 (카드 4·은행 3) + Android SMS Notification Listener |
| **카테고리 분류** | 18 카테고리, 규칙 엔진 우선 + Gemma 4 폴백 (신뢰도 < 0.7) |
| **컬러 시스템** | 크림·딥포레스트·테라코타·머스타드 (빨강·파랑 금지) |
| **타이포** | Gowun Batang (제목·서사) · Pretendard (본문·UI) |
| **모드** | 일반 / 은퇴 (L5-L6 자동) / 프리랜서 (I3 자동) |
| **법적 가이드** | 벤치마크·해석형까지만 — 종목·수익률·세무·의료 단정 모두 차단 |
| **마이데이터·투자자문** | Phase 3·Phase 4 이후 (Phase 1은 CSV·SMS 기반) |
| **클라우드** | Phase 1.5 옵트인 (월간 매거진 리포트만, 마스킹) |
| **온보딩 단계** | 7단계 (모델 다운로드 동의 §6 별도) |
| **개발 일정** | 12개월 (M1 부트스트랩 → M12 출시) |

---

## 3. 역할별 진입 가이드

### 🧭 PM이 처음 합류했다면
1. `AssetCoach_handoff_v2.md` (§제품 기획 + Phase 로드맵)
2. `AssetCoach_segment_matrix.md` (§18 세그먼트 + 18×성향 운영)
3. `AssetCoach_user_journey_v2.md` (다중 페르소나 감정 곡선)
4. **이 문서 §5 미해결 이슈** — 본인이 풀어야 할 것
5. **이 문서 §6 다음 할 일 — 합성 테스트 데이터셋·법무·CDN**

### 🎨 디자이너가 합류했다면
1. `AssetCoach_design_system.md` 전체 (526줄, 1.5시간)
2. `AssetCoach_wireframes.md` (855→946줄, 2시간) — 31 화면 사양
3. `AssetCoach_prototype.html` 브라우저로 열기 — F1~F4 흐름·세그먼트 토글 체험
4. `AssetCoach_information_architecture.md` (§홈 탭 §세그먼트별 동적 구성)
5. **할 일**: 디자인 토큰 → KMP 코드젠 통합 검토, 컴포넌트별 픽셀 사양 sign-off

### 📱 iOS 개발자가 합류했다면
1. **이 문서 §0 + §2** (60초 + 치트시트)
2. `AssetCoach_technical_architecture.md` 전체 (633→695줄)
3. 특히 §3 (Gemma 4) · §5 (DB 스키마) · §10 (성능 예산) · §11 (모듈 구성)
4. `AssetCoach_design_system.md` §디자인 토큰 (CSS 변수 → Swift enum 코드젠)
5. `AssetCoach_wireframes.md` §1·§2 (온보딩·홈)
6. **할 일**: SwiftUI 부트스트랩, MediaPipe LLM iOS 통합, URLSession 백그라운드 다운로드

### 🤖 Android 개발자가 합류했다면
1. iOS 가이드와 동일 + 다음 추가:
2. `AssetCoach_technical_architecture.md` §6.2 (SMS Notification Listener)
3. **할 일**: Compose 부트스트랩, MediaPipe LLM Android 통합, WorkManager 백그라운드 다운로드

### 🧠 ML/AI 엔지니어가 합류했다면
1. **이 문서 §0 + §2**
2. `AssetCoach_gemma4_poc.md` — **이걸 가장 먼저 실행** (1주)
3. `AssetCoach_technical_architecture.md` §3 (5개 호출 지점 A1~A5 매트릭스)
4. `AssetCoach_segment_matrix.md` §Gemma 4 시스템 프롬프트 템플릿
5. `AssetCoach_wireframes.md` §4 상담 탭 (인용 블록·추천 질문)
6. **할 일**: PoC → 프롬프트 v1 → Safety Guard 구현 → 캐시 매니저

### 🧪 QA가 합류했다면 (M9~)
1. `AssetCoach_technical_architecture.md` §14 (테스트 전략)
2. `AssetCoach_test_fixtures_spec.md` (별도 산출물 — 4번 작업)
3. `AssetCoach_segment_matrix.md` (18 세그먼트 회귀 시나리오)
4. **할 일**: 자동 회귀 + 18 세그먼트별 골든 데이터셋 검증

---

## 4. 5개 AI 호출 지점 (가장 자주 참조)

| ID | 호출 | 우선 | 컨텍스트 | 출력 | 응답시간 | 캐싱 |
|---|---|---|---|---|---|---|
| A1 | 홈 인사이트 카드 | P2 | ≤ 1.5K tok | ~80 tok × 3 | ≤ 5초 | 일 1회 batch |
| A2 | 상담 응답 | P0 | ≤ 4K tok | 150~400 tok 스트리밍 | TTFT ≤ 1.5초, TPS ≥ 20 | 거의 X |
| A3 | 주간 리포트 | P3 | ≤ 6K tok | 600~900 tok | 백그라운드 ≤ 90초 | 주1회 결과 |
| A4 | 카테고리 분류 폴백 | P1 | ≤ 200 tok | 1 tok | ≤ 300ms | 가맹점→카테 영구 |
| A5 | 추천 질문 | P3 | ≤ 1K tok | 30 tok × 5 | 백그라운드 | 영구 (95% 히트 목표) |

상세는 `AssetCoach_technical_architecture.md` §3.3.

---

## 5. 미해결 이슈 (Open Issues)

### 5.1 즉시 결정 필요
- **Gemma 4 PoC 결과** — Pass / Conditional / Fail에 따라 M1 사양 분기. 이번 주 실행 (1번 산출물 참고)
- **개발팀 구성** — 직접 채용 vs 외주 vs 하이브리드. 6명 12개월 풀타임 가정
- **CDN 인프라 견적** — 250TB 트래픽 (100K 사용자 × 2.5GB) — M9 직전까지 협상 필요

### 5.2 Phase 1 진행 중 결정
- **iOS SMS 자동 파싱 대안** — 정책상 불가, Apple Wallet 거래 API 검토 (M11)
- **모델 업데이트 OTA 채널** — CDN 업체 + 메타데이터 백엔드 (간단한 정적 호스팅)
- **익명 텔레메트리 옵트인 UX** — 기본 OFF 동의 흐름

### 5.3 Phase 2~4 (출시 후)
- **마이데이터 라이선스** (Phase 3) — 라이선스 신청 절차·요건 조사
- **투자 자문 영역** (Phase 4) — 투자자문업 등록 후 점진 확장
- **OCR 영수증 입력** (Phase 1.5) — 디자인은 카메라 권한 가드만 두고 미구현

### 5.4 잡무
- 이전 React 프로토타입(`finance_advisor_app.jsx`)과 제품 브리프 HTML이 "도리"라는 이름 — AssetCoach로 리네이밍 (현재 디렉토리에 없음, 위치 확인 필요)

---

## 6. 다음 할 일 우선순위 (Pre-M1)

| # | 작업 | 담당 | 산출물 | 시점 |
|---|---|---|---|---|
| 1 | Gemma 4 한국어 품질·성능 PoC ⭐ | ML 엔지니어 1명 | `gemma4_poc_report.md` | 이번 주 |
| 2 | 마스터 핸드오프 (이 문서) | PM | (이 문서) | 완료 |
| 3 | 프로토타입 §1.7 모델 다운로드 화면 추가 | 디자이너 | `AssetCoach_prototype.html` 갱신 | 이번 주 |
| 4 | 합성 테스트 데이터셋 사양 | PM | `AssetCoach_test_fixtures_spec.md` | 이번 달 |
| 5 | 개발팀 채용/외주 발주 사양 | PM/HR | `AssetCoach_hiring_specs.md` | 이번 달 |
| 6 | 법무 1차 검수 체크리스트 | PM + 법무 | `AssetCoach_legal_review_checklist.md` | 이번 달 |
| 7 | CSV 포맷 7종 사양·샘플 | PM | `AssetCoach_csv_format_specs.md` | 이번 달 |

---

## 7. 산출물 인덱스 (전체 파일 목록)

### 설계 문서 (1~6단계)
- `AssetCoach_handoff_v2.md` — 1단계 제품 기획
- `AssetCoach_user_journey.md` (v1) · `AssetCoach_user_journey_v2.md` (v2) — 2단계
- `AssetCoach_segment_matrix.md` — 2.5단계
- `AssetCoach_information_architecture.md` — 3단계
- `AssetCoach_design_system.md` — 4단계
- `AssetCoach_wireframes.md` — 5단계 사양
- `AssetCoach_technical_architecture.md` — 6단계

### 시각 산출물
- `AssetCoach_showcase.html` — 매거진 쇼케이스 (이해관계자 시연)
- `AssetCoach_prototype.html` — 인터랙티브 프로토타입
- `AssetCoach_app_icon.svg` · `AssetCoach_app_icon.html`

### Pre-M1 운영 문서 (이 문서 포함)
- `AssetCoach_handoff_master.md` — **이 문서**
- `AssetCoach_gemma4_poc.md` — Gemma 4 PoC 계획
- `AssetCoach_test_fixtures_spec.md` — 합성 테스트 데이터셋 사양
- `AssetCoach_hiring_specs.md` — 개발팀 채용/외주 사양
- `AssetCoach_legal_review_checklist.md` — 법무 검수 체크리스트
- `AssetCoach_csv_format_specs.md` — 카드사·은행 CSV 포맷

### 이전 자료 (참고용)
- `AssetCoach_handoff.md` (v1, 1단계 원본)

---

## 8. 빠른 의사결정 포인트

새 합류자가 가장 자주 묻는 질문 → 답:

| 질문 | 답 / 참조 |
|---|---|
| "왜 18 세그먼트인가? 더 단순하게 안 되나?" | 252 조합(6×6×7) → 한국 인구 분포·금융 행동 차이로 18로 압축. `segment_matrix.md` §1-B |
| "왜 Gemma 4인가? 더 작은 모델은?" | Apache 2.0 + 한국어 강화. 더 작은 모델은 품질 미흡. `tech_arch.md` §3.1 |
| "왜 KMP인가? React Native·Flutter는?" | 네이티브 ML 통합·플랫폼 일관성. `tech_arch.md` §2 결정 표 |
| "왜 클라우드 배제? 더 좋은 모델 쓸 수 있는데" | 프라이버시 약속 = 핵심 차별점. Phase 1.5에서 옵트인으로 검토. `handoff_v2.md` §차별점 |
| "왜 빨강·파랑 컬러 금지?" | 토스·뱅샐 신호등 컬러 회피·차분한 톤 유지. `design_system.md` §컬러 사용 규칙 |
| "왜 12개월? 더 짧게 안 되나?" | v2에서 MVP 제한 해제 + 18 세그먼트 검증 + 베타 5~6개월 포함. `handoff_v2.md` §리스크 고지 |
| "성별 기반 조언은 왜 안 하나?" | 스테레오타입 리스크 회피, 성향 3축으로 대체. `handoff_v2.md` §법적 고려 |

---

## 9. 연락·소통

- **프로젝트 디렉토리**: `/Users/sangjin/Documents/sjworkspace/AssetCoach/`
- **모든 산출물 단일 소스**: 이 디렉토리. Git 저장소 등록은 M1 부트스트랩 시
- **변경 시 갱신 의무**: §6 다음 할 일 표 + §5 미해결 이슈 — 이 두 곳은 살아있는 섹션

---

*이 문서가 오래되면 위험합니다 — 매주 갱신 권장. 특히 §5 미해결 이슈와 §6 다음 할 일.*
