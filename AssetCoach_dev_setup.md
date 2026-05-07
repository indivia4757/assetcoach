# AssetCoach Android 개발 셋업

> **단계**: M1 부트스트랩 (Phase 1)
> **작성일**: 2026-05-07
> **대상**: 솔로 개발자 또는 첫 합류 Android 개발자
> **현재 위치**: `/Users/sangjin/Documents/sjworkspace/AssetCoach/android/`

---

## 📦 생성된 프로젝트 구조

```
android/
├── settings.gradle.kts                          # 프로젝트 설정
├── build.gradle.kts                             # 루트 빌드
├── gradle.properties                            # JVM·AndroidX 설정
├── gradle/
│   ├── libs.versions.toml                       # 버전 카탈로그
│   └── wrapper/gradle-wrapper.properties        # Gradle 8.10.2
├── .gitignore
└── app/
    ├── build.gradle.kts                         # 앱 모듈 빌드 (Compose, M3)
    ├── proguard-rules.pro
    └── src/main/
        ├── AndroidManifest.xml
        ├── res/
        │   ├── values/   colors / strings / themes
        │   ├── xml/      backup / data_extraction (모두 exclude)
        │   ├── drawable/ ic_launcher_background, ic_launcher_foreground
        │   └── mipmap-anydpi-v26/ ic_launcher, ic_launcher_round
        └── java/com/assetcoach/
            ├── AssetCoachApp.kt                 # Application 진입점
            ├── MainActivity.kt                  # 단일 Activity, Compose host
            └── ui/
                ├── theme/
                │   ├── Color.kt                 # 디자인 토큰 (11 코어 + 시맨틱)
                │   ├── Type.kt                  # 11 타입 스케일
                │   ├── Spacing.kt               # 8pt 그리드 + Radius
                │   └── Theme.kt                 # M3 ColorScheme 매핑
                └── screens/
                    └── HomeScreen.kt            # S06 민호 홈 (4블록)
```

---

## 🔧 사전 준비 (한 번만)

### 1. Android Studio 설치
- **Android Studio Koala (2024.1) 이상** 권장
- https://developer.android.com/studio 에서 다운로드

### 2. SDK·JDK 확인
- **JDK 17** (Android Studio 내장 OK)
- **Android SDK 35** (Android 15) — Android Studio가 자동 설치 제안
- **Android SDK Build-Tools 35.0.0+**

### 3. 디바이스 또는 에뮬레이터
- **실기기 권장**: Galaxy S22+, Pixel 7+, 아무 Android 9+ 디바이스
  - 설정 → 휴대전화 정보 → 빌드 번호 7번 탭 → 개발자 모드 ON
  - 개발자 옵션 → USB 디버깅 ON
- **에뮬레이터 대안**: AVD Manager → Pixel 8 / Android 14 (단, ML 추론 성능 측정 불가)

---

## 🚀 첫 빌드 (5분)

### 옵션 A — Android Studio (권장)

```
1. Android Studio 실행
2. File → Open → /Users/sangjin/Documents/sjworkspace/AssetCoach/android
3. 첫 동기화 1~2분 대기 (의존성 다운로드 ~200MB)
4. 우상단 Run ▶ 클릭 → 디바이스/에뮬레이터 선택
5. 앱이 설치되고 실행됨 → S06 홈 화면 표시
```

### 옵션 B — 커맨드라인

처음에는 Gradle Wrapper jar 가 없습니다. 한 번만 생성:

```bash
cd /Users/sangjin/Documents/sjworkspace/AssetCoach/android

# 시스템 gradle 이 없다면 brew install gradle 또는 sdkman
gradle wrapper --gradle-version 8.10.2

# 이제 ./gradlew 사용 가능
./gradlew assembleDebug              # debug APK 빌드
./gradlew installDebug               # 연결된 기기에 설치
adb logcat | grep AssetCoach          # 로그 모니터
```

빌드 결과 APK:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## 🎨 첫 빌드에서 확인할 것

S06 민호의 홈 화면이 다음 4블록으로 표시되어야 합니다:

```
┌─────────────────────────────────┐
│  ⌘    에셋코치        🔔         │  ← 헤더
│                                 │
│  민호님 가족 (mustard 캡션)       │
│  이번 달 이야기예요...           │  ← Block 1 서사
│                                 │
│  ┌─ 우리 가족 재무 건강도 ──┐    │
│  │     78점                │    │  ← Block 2 위젯
│  │  ━━━━━━━━━━━━━━━━━━     │    │
│  │  S06 평균 72점보다...   │    │
│  └────────────────────────┘    │
│                                 │
│  이번 주 인사이트  3 / 7         │  ← Block 3 카드 3장
│  [중복결제] [사교육비 -5%] ...   │     가로 스와이프
│                                 │
│  진행 중인 목표  2개             │  ← Block 4 목표
│  🛡 비상금 6개월치  72%          │
│  ✈ 가족 여행 자금   45%          │
│                                 │
│  [💬 AI 코치에게 물어보기 →]     │  ← Coach CTA
└─────────────────────────────────┘
```

체크 항목:
- [ ] 크림색 배경 (#F7F3EC)
- [ ] 테라코타 강조 (게이지·CTA)
- [ ] 머스타드 보더 (재무 건강도 카드)
- [ ] 강조 표현 "34만원 덜" 형광펜 효과
- [ ] 인사이트 카드 좌측 컬러 스트라이프 (테라코타·초록·머스타드)
- [ ] 진행률 바 정확 (72% / 45%)

**미적용 사항** (의도적, Phase 1.5에서 추가):
- Gowun Batang / Pretendard 한글 폰트 — 현재 시스템 폰트로 대체
- 종이 질감 노이즈 오버레이
- 카드 진입 애니메이션
- 탭바 (홈·분석·상담·목표) — 단일 화면만 구현

---

## 🛣 Phase 별 다음 작업 로드맵

### Phase 1 (지금) — UI 골격 ✅
- [x] Gradle 부트스트랩
- [x] 디자인 토큰 (Color / Type / Spacing)
- [x] M3 Theme 매핑
- [x] S06 홈 화면

### Phase 1.5 — UI 보강 (1~2주)
- [ ] Google Fonts 통합 (Gowun Batang + Pretendard)
- [ ] 종이 질감 노이즈 오버레이 (`Modifier.drawWithCache` + 타일링 비트맵)
- [ ] 4탭 네비게이션 (Compose Navigation)
- [ ] S04 / S17 홈 변주 추가
- [ ] 분석·상담·목표 탭 placeholder

### Phase 2 — 데이터 레이어 (2~3주)
1. `app/build.gradle.kts` 의 Phase 2 의존성 주석 해제:
   ```kotlin
   implementation("net.zetetic:android-database-sqlcipher:4.5.4")
   implementation("androidx.room:room-runtime:2.6.1")
   ```
2. `data/db/` 디렉토리 생성
3. `AssetCoachDatabase.kt` Room + SQLCipher
4. Entity: Transaction / Category / UserProfile / Goal
5. CSV 파서 (신한카드 우선) + golden test
6. 카테고리 분류 룰 엔진 (500+ 시드 매핑)

### Phase 3 — Gemma 4 추론 (2~3주)
1. `app/build.gradle.kts` Phase 3 의존성 해제:
   ```kotlin
   implementation("com.google.mediapipe:tasks-genai:0.10.14")
   ```
2. `ai/` 디렉토리 생성
3. `GemmaClient.kt` — MediaPipe LLM Inference 래퍼
4. 모델 OTA 다운로드 (WorkManager) + SHA-256 검증
5. 시스템 프롬프트 v1 (S06 우선)
6. A2 상담 응답 — 채팅 UI 통합
7. Safety Guard (금기 표현 회피)

### Phase 4 — SMS 파서 + 백그라운드 (1~2주)
1. AndroidManifest.xml SMS Listener 권한 해제
2. `NotificationListenerService` 구현
3. 정규식 매칭 + LLM 폴백 (A4)
4. WorkManager 주간 리포트 스케줄

---

## 🐛 자주 만나는 첫 빌드 이슈

| 증상 | 해결 |
|---|---|
| `Plugin not found: com.android.application` | `settings.gradle.kts` 의 pluginManagement 블록 확인. 첫 sync 시 인터넷 필요 |
| Compose 컴파일러 버전 오류 | Kotlin 2.0+ 사용. `libs.versions.toml` `kotlin = "2.0.20"` 확인 |
| `Could not resolve com.google.mediapipe:tasks-genai` | Phase 3 시점까지는 주석 처리됨. 의존성 해제는 Phase 3에서 |
| 한글이 □ 로 표시 | 시스템 폰트 fallback이 한글 안 됨 — Phase 1.5 Google Fonts 통합 필요 |
| `INSTALL_FAILED_USER_RESTRICTED` | 디바이스에서 USB 디버깅 + "이 컴퓨터 신뢰" 허용 |
| 에뮬레이터에서 매우 느림 | x86_64 또는 arm64 시스템 이미지 사용. 또는 실기기 권장 |

---

## 📝 개발 워크플로우 (권장)

```
1. 새 기능 브랜치: git checkout -b feat/<기능>
2. 코드 변경 → Android Studio Run ▶ 또는 ./gradlew installDebug
3. 디바이스에서 검증 → 디자인 시스템 사양과 비교 (와이어프레임 §X.X)
4. 단위 테스트: ./gradlew testDebugUnitTest
5. 커밋 → main 머지
```

**Git 초기화** (아직 안 했다면):
```bash
cd /Users/sangjin/Documents/sjworkspace/AssetCoach/android
git init
git add .
git commit -m "M1 Phase 1: bootstrap + design tokens + S06 home"
```

---

## 🎯 성공 기준 (Phase 1 완료)

- [ ] 디바이스에 앱 설치 성공
- [ ] S06 홈 화면 정상 표시
- [ ] 디자인 토큰 정확 (색상·간격·라운딩)
- [ ] 4블록 구조 와이어프레임 §2.4와 일치
- [ ] 인사이트 카드 가로 스와이프 작동
- [ ] APK 사이즈 < 10 MB (모델 없이)

이게 다 되면 Phase 1.5 → Phase 2 순서로 진행.

---

## 📚 참조 문서

- `AssetCoach_design_system.md` — 디자인 토큰·컴포넌트 사양
- `AssetCoach_wireframes.md` — 31화면 사양 (특히 §2.4 S06 홈)
- `AssetCoach_technical_architecture.md` — 전체 기술 청사진
- `AssetCoach_handoff_master.md` — 마스터 진입점

---

*솔로 개발 시 Phase 1.5 → Phase 2 → Phase 3 순서 권장. Gemma 4 통합(Phase 3) 전에 UI·데이터가 안정되어 있어야 디버깅이 쉽습니다.*
