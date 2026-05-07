package com.assetcoach

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.assetcoach.domain.model.HomeData
import com.assetcoach.domain.model.HomeMockData
import com.assetcoach.security.LockState
import com.assetcoach.ui.nav.AppNavigation
import com.assetcoach.ui.screens.LockScreen
import com.assetcoach.ui.screens.OnboardingScreen
import com.assetcoach.ui.theme.AssetCoachTheme
import com.assetcoach.ui.theme.Cream
import com.assetcoach.ui.theme.DisplayMode

/**
 * 단일 Activity (FragmentActivity — BiometricPrompt 호스트).
 *
 * 진입 흐름:
 *   콜드 스타트 → (LockScreen 옵션) → 프로필 없으면 Onboarding → 있으면 AppNavigation
 */
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as AssetCoachApp

        setContent {
            AppRoot(app = app)
        }
    }
}

@Composable
private fun AppRoot(app: AssetCoachApp) {
    val lockState by app.lockManager.state.collectAsStateWithLifecycle()
    val profile by app.userProfileRepository.observe().collectAsStateWithLifecycle(initialValue = null)

    // 세그먼트 결정 — 프로필 있으면 그 segmentId, 없으면 mock S06 (온보딩 중)
    val resolvedData = if (profile != null) {
        // 등록된 프로필 → mock data 중 매칭하는 것 사용 (Phase 5: ViewModel 이 DB 거래로 동적 생성)
        HomeMockData.all.firstOrNull { it.segmentId == profile?.segmentId }
            ?: HomeMockData.s06.copy(
                segmentId = profile?.segmentId ?: "S06",
                nameLabel = profile?.nameLabel ?: "사용자"
            )
    } else {
        HomeMockData.s06
    }

    val mode = when (resolvedData.displayMode) {
        HomeData.DisplayModeKind.Retirement -> DisplayMode.Retirement
        HomeData.DisplayModeKind.Freelance -> DisplayMode.Freelance
        else -> DisplayMode.Normal
    }

    AssetCoachTheme(displayMode = mode) {
        Surface(
            modifier = Modifier.fillMaxSize().background(Cream),
            color = Cream
        ) {
            when {
                lockState is LockState.Locked -> {
                    LockScreen(onUnlock = { app.lockManager.unlock() })
                }
                profile == null -> {
                    OnboardingScreen(onComplete = { /* DB 갱신 시 자동 재컴포지션 */ })
                }
                else -> {
                    AppNavigation(
                        homeData = resolvedData,
                        onProfileTap = { /* Phase 5: 설정 진입 */ },
                        onNotifTap = { /* Phase 5: 알림 화면 */ }
                    )
                }
            }
        }
    }
}
