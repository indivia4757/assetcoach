package com.assetcoach.ui.nav

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.assetcoach.domain.model.HomeData
import com.assetcoach.ui.screens.AnalysisScreen
import com.assetcoach.ui.screens.ChatScreen
import com.assetcoach.ui.screens.HomeScreen
import com.assetcoach.ui.theme.AppType
import com.assetcoach.ui.theme.Cream
import com.assetcoach.ui.theme.Forest
import com.assetcoach.ui.theme.Neutral

/**
 * 4탭 하단 네비게이션 (와이어프레임 §글로벌 네비게이션).
 * 홈 / 분석 / 상담 / 목표 — 분석/상담/목표는 현재 placeholder.
 * Phase 2 부터 분석 탭에 실제 거래 데이터 연결.
 */
private enum class AppTab(val route: String, val label: String, val icon: String) {
    Home("home", "홈", "⌂"),
    Analysis("analysis", "분석", "▤"),
    Chat("chat", "상담", "◌"),
    Goals("goals", "목표", "◇")
}

@Composable
fun AppNavigation(
    homeData: HomeData,
    onProfileTap: () -> Unit,
    onNotifTap: () -> Unit
) {
    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    Scaffold(
        containerColor = Cream,
        bottomBar = {
            NavigationBar(
                containerColor = Cream,
                tonalElevation = 0.dp
            ) {
                AppTab.entries.forEach { tab ->
                    val selected = currentRoute == tab.route ||
                        currentBackStack?.destination?.hierarchy?.any { it.route == tab.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Text(tab.icon, style = AppType.bodyLg) },
                        label = { Text(tab.label, style = AppType.caption) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Forest,
                            selectedTextColor = Forest,
                            indicatorColor = Cream,
                            unselectedIconColor = Neutral,
                            unselectedTextColor = Neutral
                        )
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = AppTab.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            composable(AppTab.Home.route) {
                HomeScreen(
                    onProfileClick = onProfileTap,
                    onNotificationClick = onNotifTap,
                    data = homeData
                )
            }
            composable(AppTab.Analysis.route) { AnalysisScreen() }
            composable(AppTab.Chat.route) {
                ChatScreen(segmentId = homeData.segmentId, nameLabel = homeData.nameLabel)
            }
            composable(AppTab.Goals.route) { PlaceholderScreen("목표") }
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$title — 곧 만나요",
            style = AppType.h2.copy(color = Forest)
        )
    }
}
