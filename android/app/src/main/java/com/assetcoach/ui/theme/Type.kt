package com.assetcoach.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.assetcoach.R

// ─────────────────────────────────────────────────────────
// AssetCoach Design System v1.0 — Typography
// 출처: AssetCoach_design_system.md §타이포그래피
//
// Phase 1.5: Gowun Batang + Pretendard 번들. APK +22MB 추가.
// 향후 Phase 2에서 한글 subset 으로 사이즈 최적화 검토.
// ─────────────────────────────────────────────────────────

val GowunBatang = FontFamily(
    Font(R.font.gowun_batang_regular, FontWeight.Normal),
    Font(R.font.gowun_batang_bold, FontWeight.Bold)
)

val Pretendard = FontFamily(
    Font(R.font.pretendard_regular, FontWeight.Normal),
    Font(R.font.pretendard_medium, FontWeight.Medium),
    Font(R.font.pretendard_semibold, FontWeight.SemiBold),
    Font(R.font.pretendard_bold, FontWeight.Bold)
)

object AssetCoachType {
    val display = TextStyle(
        fontFamily = GowunBatang, fontWeight = FontWeight.Bold,
        fontSize = 48.sp, lineHeight = 51.sp, letterSpacing = (-0.96).sp
    )
    val h1 = TextStyle(
        fontFamily = GowunBatang, fontWeight = FontWeight.Bold,
        fontSize = 32.sp, lineHeight = 37.sp, letterSpacing = (-0.48).sp
    )
    val h2 = TextStyle(
        fontFamily = GowunBatang, fontWeight = FontWeight.Bold,
        fontSize = 24.sp, lineHeight = 29.sp, letterSpacing = (-0.24).sp
    )
    val narrative = TextStyle(
        fontFamily = GowunBatang, fontWeight = FontWeight.Normal,
        fontSize = 20.sp, lineHeight = 28.sp, letterSpacing = (-0.1).sp
    )
    val bodyLg = TextStyle(
        fontFamily = Pretendard, fontWeight = FontWeight.Normal,
        fontSize = 17.sp, lineHeight = 26.sp
    )
    val body = TextStyle(
        fontFamily = Pretendard, fontWeight = FontWeight.Normal,
        fontSize = 15.sp, lineHeight = 23.sp
    )
    val bodySm = TextStyle(
        fontFamily = Pretendard, fontWeight = FontWeight.Normal,
        fontSize = 13.sp, lineHeight = 20.sp
    )
    val caption = TextStyle(
        fontFamily = Pretendard, fontWeight = FontWeight.Medium,
        fontSize = 11.sp, lineHeight = 15.sp, letterSpacing = 0.22.sp
    )
    val numXl = TextStyle(
        fontFamily = Pretendard, fontWeight = FontWeight.Bold,
        fontSize = 36.sp, lineHeight = 36.sp, letterSpacing = (-0.9).sp
    )
    val numLg = TextStyle(
        fontFamily = Pretendard, fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp, lineHeight = 26.sp, letterSpacing = (-0.36).sp
    )
    val num = TextStyle(
        fontFamily = Pretendard, fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp, lineHeight = 22.sp
    )
}

val AssetCoachTypography = Typography(
    displayLarge = AssetCoachType.display,
    headlineLarge = AssetCoachType.h1,
    headlineMedium = AssetCoachType.h2,
    titleLarge = AssetCoachType.narrative,
    bodyLarge = AssetCoachType.bodyLg,
    bodyMedium = AssetCoachType.body,
    bodySmall = AssetCoachType.bodySm,
    labelMedium = AssetCoachType.caption
)

// ─────────────────────────────────────────────────────────
// Retirement Mode (S17 등 L5-L6 자동 적용)
// 사이즈 +20%, 행간 +10%
// 출처: design system §은퇴 모드
// ─────────────────────────────────────────────────────────

private fun TextStyle.scale(factor: Float): TextStyle = copy(
    fontSize = fontSize * factor,
    lineHeight = lineHeight * (1 + (factor - 1) * 0.5f)
)

object AssetCoachTypeRetirement {
    val display = AssetCoachType.display.scale(1.2f)
    val h1 = AssetCoachType.h1.scale(1.2f)
    val h2 = AssetCoachType.h2.scale(1.2f)
    val narrative = AssetCoachType.narrative.scale(1.2f)
    val bodyLg = AssetCoachType.bodyLg.scale(1.2f)
    val body = AssetCoachType.body.scale(1.2f)
    val bodySm = AssetCoachType.bodySm.scale(1.15f)
    val caption = AssetCoachType.caption.scale(1.15f)
    val numXl = AssetCoachType.numXl.scale(1.25f)
    val numLg = AssetCoachType.numLg.scale(1.2f)
    val num = AssetCoachType.num.scale(1.2f)
}

val AssetCoachTypographyRetirement = Typography(
    displayLarge = AssetCoachTypeRetirement.display,
    headlineLarge = AssetCoachTypeRetirement.h1,
    headlineMedium = AssetCoachTypeRetirement.h2,
    titleLarge = AssetCoachTypeRetirement.narrative,
    bodyLarge = AssetCoachTypeRetirement.bodyLg,
    bodyMedium = AssetCoachTypeRetirement.body,
    bodySmall = AssetCoachTypeRetirement.bodySm,
    labelMedium = AssetCoachTypeRetirement.caption
)
