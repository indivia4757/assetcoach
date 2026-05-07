package com.assetcoach

import android.os.Bundle
import androidx.activity.ComponentActivity
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
import com.assetcoach.domain.model.HomeData
import com.assetcoach.domain.model.HomeMockData
import com.assetcoach.ui.nav.AppNavigation
import com.assetcoach.ui.theme.AssetCoachTheme
import com.assetcoach.ui.theme.Cream
import com.assetcoach.ui.theme.DisplayMode

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Phase 1.5 dev affordance: 프로필 아이콘 탭으로 세그먼트 사이클.
            // Phase 2 에서 사용자 프로파일·세그먼트 자동 진단으로 대체.
            var segmentIdx by remember { mutableIntStateOf(0) }
            val data = HomeMockData.all[segmentIdx % HomeMockData.all.size]

            val mode = when (data.displayMode) {
                HomeData.DisplayModeKind.Retirement -> DisplayMode.Retirement
                HomeData.DisplayModeKind.Freelance -> DisplayMode.Freelance
                else -> DisplayMode.Normal
            }

            AssetCoachTheme(displayMode = mode) {
                Surface(
                    modifier = Modifier.fillMaxSize().background(Cream),
                    color = Cream
                ) {
                    AppNavigation(
                        homeData = data,
                        onProfileTap = { segmentIdx = (segmentIdx + 1) % HomeMockData.all.size },
                        onNotifTap = { /* Phase 2: 알림 화면 */ }
                    )
                }
            }
        }
    }
}
