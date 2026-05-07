package com.assetcoach

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.assetcoach.domain.model.HomeData
import com.assetcoach.domain.model.HomeMockData
import com.assetcoach.security.LockState
import com.assetcoach.ui.nav.AppNavigation
import com.assetcoach.ui.screens.LockScreen
import com.assetcoach.ui.theme.AssetCoachTheme
import com.assetcoach.ui.theme.Cream
import com.assetcoach.ui.theme.DisplayMode

/**
 * 단일 Activity (FragmentActivity — BiometricPrompt 호스트).
 *
 * 진입 흐름:
 *   콜드 스타트 → LockScreen → 생체 인증 → AppNavigation
 */
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as AssetCoachApp

        setContent {
            // Phase 1.5 dev affordance: 프로필 아이콘 탭으로 세그먼트 사이클.
            var segmentIdx by remember { mutableIntStateOf(0) }
            val data = HomeMockData.all[segmentIdx % HomeMockData.all.size]

            val mode = when (data.displayMode) {
                HomeData.DisplayModeKind.Retirement -> DisplayMode.Retirement
                HomeData.DisplayModeKind.Freelance -> DisplayMode.Freelance
                else -> DisplayMode.Normal
            }

            val lockState by app.lockManager.state.collectAsStateWithLifecycle()

            AssetCoachTheme(displayMode = mode) {
                Surface(
                    modifier = Modifier.fillMaxSize().background(Cream),
                    color = Cream
                ) {
                    when (lockState) {
                        LockState.Locked -> {
                            LockScreen(onUnlock = { app.lockManager.unlock() })
                        }
                        LockState.Unlocked -> {
                            AppNavigation(
                                homeData = data,
                                onProfileTap = {
                                    segmentIdx = (segmentIdx + 1) % HomeMockData.all.size
                                },
                                onNotifTap = { /* Phase 4: 알림 화면 */ }
                            )
                        }
                    }
                }
            }
        }
    }
}
