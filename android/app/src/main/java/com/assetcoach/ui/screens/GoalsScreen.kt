package com.assetcoach.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.assetcoach.ui.theme.AppType
import com.assetcoach.ui.theme.Cream
import com.assetcoach.ui.theme.CreamDeep
import com.assetcoach.ui.theme.Faint
import com.assetcoach.ui.theme.Faint3
import com.assetcoach.ui.theme.Forest
import com.assetcoach.ui.theme.ForestSoft
import com.assetcoach.ui.theme.GowunBatang
import com.assetcoach.ui.theme.Ink
import com.assetcoach.ui.theme.MustardDeep
import com.assetcoach.ui.theme.Neutral
import com.assetcoach.ui.theme.Paper
import com.assetcoach.ui.theme.Positive
import com.assetcoach.ui.theme.Radius
import com.assetcoach.ui.theme.Spacing
import com.assetcoach.ui.theme.Terracotta
import com.assetcoach.ui.theme.paperNoise

/**
 * 목표 탭 — 와이어프레임 §5.1.
 *
 * Phase 7 시점: 정적 mock 목표 + "달성" 섹션 + "추가" CTA.
 * Phase 7+ 후속에서 GoalEntity + DAO + ViewModel 추가.
 */
@Composable
fun GoalsScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .paperNoise()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = Spacing.s5, vertical = Spacing.s5),
            verticalArrangement = Arrangement.spacedBy(Spacing.s4)
        ) {
            item {
                Column(modifier = Modifier.padding(top = Spacing.s5, bottom = Spacing.s2)) {
                    Text(
                        "목표",
                        style = AppType.h1.copy(
                            color = Forest,
                            fontFamily = GowunBatang,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(Modifier.height(Spacing.s1))
                    Text(
                        "함께 키워가는 자산",
                        style = AppType.body.copy(color = ForestSoft)
                    )
                }
            }

            item { SectionHeader("진행 중", "3개") }

            item {
                GoalCard(
                    emoji = "🛡",
                    title = "비상금 6개월치",
                    current = 720_0000,
                    target = 1000_0000,
                    monthlyAdd = "매달 28만원 적립 중",
                    eta = "8개월 후 달성 예정",
                    accentMustard = false
                )
            }
            item {
                GoalCard(
                    emoji = "✈",
                    title = "가족 여행 자금",
                    current = 45_0000,
                    target = 100_0000,
                    monthlyAdd = "매달 12만원 적립 중",
                    eta = "5개월 후 달성 예정",
                    accentMustard = true
                )
            }
            item {
                GoalCard(
                    emoji = "🎓",
                    title = "자녀 학자금",
                    current = 1500_0000,
                    target = 5000_0000,
                    monthlyAdd = "매달 50만원 적립 중",
                    eta = "5년 10개월 후 달성 예정",
                    accentMustard = false
                )
            }

            item { SectionHeader("추천 목표", "S06 영유아 가구") }
            item {
                RecommendCard(emoji = "🛡", title = "가족 보장(보험) 점검")
            }
            item {
                RecommendCard(emoji = "📝", title = "비상금 12개월치 확장")
            }

            item {
                Spacer(Modifier.height(Spacing.s5))
                AddGoalCta()
            }

            item { Spacer(Modifier.height(Spacing.s7)) }
        }
    }
}

@Composable
private fun SectionHeader(name: String, meta: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Spacing.s2, bottom = Spacing.s1),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            name,
            style = AppType.bodyLg.copy(
                fontFamily = GowunBatang,
                fontWeight = FontWeight.Bold,
                color = Forest
            )
        )
        Text(meta, style = AppType.caption.copy(color = Neutral))
    }
}

@Composable
private fun GoalCard(
    emoji: String,
    title: String,
    current: Long,
    target: Long,
    monthlyAdd: String,
    eta: String,
    accentMustard: Boolean
) {
    val pct = (current.toFloat() / target).coerceIn(0f, 1f)
    val pctText = "${(pct * 100).toInt()}%"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.r3))
            .background(Paper)
            .border(1.dp, Faint, RoundedCornerShape(Radius.r3))
            .padding(Spacing.s4)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CreamDeep),
                contentAlignment = Alignment.Center
            ) { Text(emoji, style = AppType.bodyLg) }
            Spacer(Modifier.size(Spacing.s3))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = AppType.bodyLg.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Forest
                    )
                )
                Text(
                    "${formatCompactWon(current)} / ${formatCompactWon(target)}",
                    style = AppType.caption.copy(color = Neutral)
                )
            }
            Text(
                pctText,
                style = AppType.numLg.copy(color = Ink, fontWeight = FontWeight.Bold)
            )
        }
        Spacer(Modifier.height(Spacing.s3))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Faint)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(pct)
                    .fillMaxHeight()
                    .background(if (accentMustard) MustardDeep else Terracotta)
            )
        }
        Spacer(Modifier.height(Spacing.s3))
        Text(monthlyAdd, style = AppType.bodySm.copy(color = ForestSoft))
        Text(eta, style = AppType.caption.copy(color = Neutral))
    }
}

@Composable
private fun RecommendCard(emoji: String, title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.r3))
            .background(Paper)
            .border(1.dp, Faint, RoundedCornerShape(Radius.r3))
            .clickable { /* Phase 7+: 추가 흐름 */ }
            .padding(horizontal = Spacing.s4, vertical = Spacing.s3),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.s3)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(CreamDeep),
            contentAlignment = Alignment.Center
        ) { Text(emoji, style = AppType.bodyLg) }
        Text(
            title,
            style = AppType.body.copy(color = Forest, fontWeight = FontWeight.Medium),
            modifier = Modifier.weight(1f)
        )
        Text("+", style = AppType.bodyLg.copy(color = Forest, fontWeight = FontWeight.Bold))
    }
}

@Composable
private fun AddGoalCta() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.r3))
            .background(Forest)
            .clickable { /* Phase 7+: 목표 추가 화면 */ }
            .padding(vertical = Spacing.s4, horizontal = Spacing.s4),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.s2)
    ) {
        Text("✚", style = AppType.bodyLg.copy(color = Cream, fontWeight = FontWeight.Bold))
        Text(
            "새 목표 추가",
            style = AppType.bodyLg.copy(color = Cream, fontWeight = FontWeight.SemiBold),
            modifier = Modifier.weight(1f)
        )
    }
}

private fun formatCompactWon(amount: Long): String {
    val abs = kotlin.math.abs(amount)
    return when {
        abs >= 10_000 -> "%,d만".format(abs / 10_000)
        else -> "%,d원".format(abs)
    }
}
