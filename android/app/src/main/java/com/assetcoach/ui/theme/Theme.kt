package com.assetcoach.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Display mode — 와이어프레임 §모드 시스템 + 디자인 시스템 §은퇴 모드.
 *
 * - Normal: 기본
 * - Retirement: L5-L6 자동, 폰트 +20% / 차트 1개 제한
 * - Freelance: I3 자동, 분기 토글 우선
 */
enum class DisplayMode { Normal, Retirement, Freelance }

val LocalDisplayMode = staticCompositionLocalOf { DisplayMode.Normal }

/**
 * 활성 타입스타일 셋. retirement 모드일 때 자동으로 +20% 스케일 적용.
 */
object AppType {
    val display @Composable get() = if (LocalDisplayMode.current == DisplayMode.Retirement)
        AssetCoachTypeRetirement.display else AssetCoachType.display
    val h1 @Composable get() = if (LocalDisplayMode.current == DisplayMode.Retirement)
        AssetCoachTypeRetirement.h1 else AssetCoachType.h1
    val h2 @Composable get() = if (LocalDisplayMode.current == DisplayMode.Retirement)
        AssetCoachTypeRetirement.h2 else AssetCoachType.h2
    val narrative @Composable get() = if (LocalDisplayMode.current == DisplayMode.Retirement)
        AssetCoachTypeRetirement.narrative else AssetCoachType.narrative
    val bodyLg @Composable get() = if (LocalDisplayMode.current == DisplayMode.Retirement)
        AssetCoachTypeRetirement.bodyLg else AssetCoachType.bodyLg
    val body @Composable get() = if (LocalDisplayMode.current == DisplayMode.Retirement)
        AssetCoachTypeRetirement.body else AssetCoachType.body
    val bodySm @Composable get() = if (LocalDisplayMode.current == DisplayMode.Retirement)
        AssetCoachTypeRetirement.bodySm else AssetCoachType.bodySm
    val caption @Composable get() = if (LocalDisplayMode.current == DisplayMode.Retirement)
        AssetCoachTypeRetirement.caption else AssetCoachType.caption
    val numXl @Composable get() = if (LocalDisplayMode.current == DisplayMode.Retirement)
        AssetCoachTypeRetirement.numXl else AssetCoachType.numXl
    val numLg @Composable get() = if (LocalDisplayMode.current == DisplayMode.Retirement)
        AssetCoachTypeRetirement.numLg else AssetCoachType.numLg
    val num @Composable get() = if (LocalDisplayMode.current == DisplayMode.Retirement)
        AssetCoachTypeRetirement.num else AssetCoachType.num
}

private val LightColorScheme = lightColorScheme(
    primary = Terracotta,
    onPrimary = Cream,
    primaryContainer = Highlight,
    onPrimaryContainer = TerracottaDeep,

    secondary = Mustard,
    onSecondary = Forest,
    secondaryContainer = CreamDeep,
    onSecondaryContainer = Forest,

    tertiary = Positive,
    onTertiary = Cream,
    tertiaryContainer = PositiveBg,
    onTertiaryContainer = Forest,

    background = Cream,
    onBackground = Forest,
    surface = Paper,
    onSurface = Forest,
    surfaceVariant = CreamDeep,
    onSurfaceVariant = ForestSoft,

    outline = Faint2,
    outlineVariant = Faint,

    error = Negative,
    onError = Cream,
    errorContainer = NegativeBg,
    onErrorContainer = TerracottaDeep
)

@Composable
fun AssetCoachTheme(
    displayMode: DisplayMode = DisplayMode.Normal,
    content: @Composable () -> Unit
) {
    val typography = if (displayMode == DisplayMode.Retirement) {
        AssetCoachTypographyRetirement
    } else {
        AssetCoachTypography
    }
    CompositionLocalProvider(LocalDisplayMode provides displayMode) {
        MaterialTheme(
            colorScheme = LightColorScheme,
            typography = typography,
            content = content
        )
    }
}
